import os
import urllib
import logging
from google.appengine.ext import webapp
from google.appengine.api import users
from google.appengine.api import memcache
from google.appengine.api import urlfetch
from google.appengine.ext.webapp import template

from customexceptions import C2DMQuotaExceedError

import django.utils.simplejson as json

from scenedb.c2dmusers import C2dmUser

class RegisterDevice(webapp.RequestHandler):
  
  def post(self):
    
    reqRegId    = self.request.get('registrationId')
    reqDeviceId = self.request.get('deviceId')
    response    = "";
    if reqRegId and reqDeviceId and \
      len(reqRegId) > 0 and len(reqDeviceId) > 0 :
      #save the deviceId and registrationId
      c2dmUser = C2dmUser(deviceId=reqDeviceId, regId=reqRegId);
      c2dmUser.put();
      #write a json success response for the device
      response = json.dumps({'responsetype':'success'})
    else:
      #write a json invalid request response for device
      response = json.dumps({'responsetype':'error'})
    self.response.out.write(response)
    

class PostMessage(webapp.RequestHandler):
  
  AUTH_TOKEN_KEY = "auth_token"
  AUTH_URL       = "https://www.google.com/accounts/ClientLogin"
  PUSH_URL       = "https://android.apis.google.com/c2dm/send"
  AUTH_TOKEN     = None
  
  def post(self, city = None):
    #this request can only be executed by admin
    #like he press a button and all devices of presence of new data on server
    
    #check if admin
    user = users.get_current_user()
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #current user is admin
      #retrieve google auth token
      PostMessage.AUTH_TOKEN = self.getGoogleAuthToken()
      
      if PostMessage.AUTH_TOKEN is None:
        #can't retrieve token try after sometime
        logging.error("authentication token retrieval failed")
        template_values = {
         'errorMsg' : "can't retrieve auth token try after sometime"
        }
        path = os.path.join(rootPath, "html/error.html")
      else:
        #use the retrieved auth_token to notify all reg devices db update
        logging.info("notify devices of update")
        C2dmUsers_query = C2dmUser.all()
        if city is not None:
          logging.info("fetching devices with city %s", city)
          C2dmUsers_query.filter("city =", city)
        try:
          for c2dmUser in C2dmUsers_query:
            #push msg to each device
            self.notifyDevice(c2dmUser.regId, c2dmUser.deviceId)
          #all devices pushed msgs
          template_values = {
           'successMsg' : "messages pushed"
          }
          path = os.path.join(rootPath, "html/new_gig.html")
        except C2DMQuotaExceedError as e:
          template_values = {
           'errorMsg' : "c2dm quota exceeded "+e.errorCode
          }
          path = os.path.join(rootPath, "html/error.html")
    else:
      #current user is not admin
      logging.error("not sufficient privileges")
      template_values = {
         'errorMsg' : "Not sufficient privileges."
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))

  def refreshGoogleAuthToken(self):
    memcache.delete(PostMessage.AUTH_TOKEN_KEY)
    return self.getGoogleAuthToken()

  def notifyDevice(self, regId, deviceId):
    #notify this device
    logging.info("notify device# %s reg# ", deviceId, regId)
    pushParameters = {
      "registration_id":regId,
      "collapse_key":deviceId,
      "data.update":"true",
      "delay_while_idle":"true"
    }
    reqAuthToken = memcache.get(PostMessage.AUTH_TOKEN_KEY)
    
    if reqAuthToken is None:
      reqAuthToken = PostMessage.AUTH_TOKEN
      
    form_data = urllib.urlencode(pushParameters)
    response = urlfetch.fetch(url=PostMessage.PUSH_URL, 
                              payload=form_data, 
                              method=urlfetch.POST, 
                              headers={
                                       'Authorization':'GoogleLogin auth='+reqAuthToken,
                                       'Content-Type':'application/x-www-form-urlencoded'
                                       })
    
    logging.debug("notifyDevice response: %s", response.status_code)
    
    if response.status_code == 200:
      #succesful response search for line "id=" and "Error="
      responseContent = response.content
      respContentLines = responseContent.splitlines()
      for line in respContentLines:
        if line.startswith("id="):
          logging.info("device notified with msg %s", line[len('id='):])
          break
        else:
          #some error encountered
          logging.error("error in notifying device")
          if line.startswith("Error="):
            #identify error and take action on it
            errorCode = line[len('Error='):]+''
            logging.error('errorcode: %s', errorCode)
            if 'InvalidRegistration'.lower() == errorCode or \
              'NotRegistered'.lower() == errorCode:
              #remove this registrationId
              self.removeC2DMUser(regId)
            elif 'QuotaExceeded '.lower() == errorCode or \
              'DeviceQuotaExceeded '.lower() == errorCode:
              raise C2DMQuotaExceedError(errorCode)
    elif response.status_code == 401:
      #need to refresh authentication token
      logging.error("authentication token used is invalid, try to refresh it")
      PostMessage.AUTH_TOKEN = self.refreshGoogleAuthToken()
    else:
      #some other error
      logging.error("some error")
  
  def removeC2DMUser(self, regId):
    c2dmUser = C2dmUser.all().filter('regId=', regId).fetch(1)
    if len(c2dmUser) > 0:
      c2dmUser.delete()
    
    
  
  def getGoogleAuthToken(self):
    
    auth_token = memcache.get(PostMessage.AUTH_TOKEN_KEY)
    
    if auth_token is None:
      #initiate request for google auth token
      authRequestParameters = {
        "accountType":"HOSTED_OR_GOOGLE",
        "Email":"xxxx",
        "Passwd":"xxxx",
        "service":"ac2dm",
        "source":"bandbaaja-bandbaaja-1.0",
      }
      
      form_data = urllib.urlencode(authRequestParameters)
      
      response = urlfetch.fetch(url=PostMessage.AUTH_URL,
                                 payload=form_data,
                                 method=urlfetch.POST,
                                 headers={'Content-Type':'application/x-www-form-urlencoded'})
      if response.status_code == 200:
        #successful response parse for Auth=token line
        responseContent = response.content
        respContentLines = responseContent.splitlines()
        for line in respContentLines:
          if line.startswith(('auth=', 'Auth=')):
            auth_token = line[len('auth='):]
            break
          
        #cache the token for future use
        if not memcache.add(PostMessage.AUTH_TOKEN_KEY, auth_token):
          #memcache addition not successful
          logging.error("memcache addition of authentication token failed")
      else:
        #failure in retrieving auth token
          logging.error("authentication token retrieval failed")
    return auth_token