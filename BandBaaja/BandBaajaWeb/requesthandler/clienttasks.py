import django.utils.simplejson as json 
import os
import cgi
import logging
from google.appengine.ext import webapp
from google.appengine.api import users
from scenedb.gig import Gig
from scenedb.feedback import Feedback
from google.appengine.ext.webapp import template
from datetime import datetime
from gigjsonencoder import GigJsonEncoder

"""
    sample json request
    {
      "city":"bangalore",
      "recentGig":"id_of_last_recent_gig_sent_by_server" 
    }
"""
"""
    sample json response
    {
      "city":"bangalore",
      "recentGigId":"id_of_last_recent_gig_sent_by_server",
      "isdelete":"false",
      "gigs":[
                {
                  "venue":"",
                  "artist":"",
                  "city_loc":"",
                  "city":"",
                  "latlong":"",
                  "event_url":"",
                  "genre":"",
                  "description":"",
                  "bookmarked":"",
                  "date_time":"" #2004-04-12 20:30
                },
                {
                  "venue":"",
                  "artist":"",
                  "city_loc":"",
                  "city":"",
                  "latlong":"",
                  "event_url":"",
                  "genre":"",
                  "description":"",
                  "bookmarked":"",
                  "date_time":""
                },
                {
                  ....
                  ....
                  ...
                }
            ]
    }
"""
class ClientGetGigs(webapp.RequestHandler):
  
  CLIENT_GET_LIMIT = 100
  
  def post(self):
    #parse the json request
    #strRequestJson = '{"city":"bangalore", "recentGig":""}'
    strRequestJson = self.request.get('request')
    resp = ''
    try:
      pyRequestJson   = json.loads(strRequestJson)
      city            = pyRequestJson['city']
      recentGigId     = pyRequestJson['recentGig']
      if not(city and len(city) > 0):
        #invalid JSON/raise an exception which will be caught below and handled
        logging.error("requested invalid city")
        raise ValueError
      else:
        #valid input
        validRecentGig =  self.validateRecentGigId(recentGigId)
        gigs     = self.getGigs(city, validRecentGig)
        jsonResp = self.buildResponseJson(gigs, city, 
                                            False if validRecentGig else True)  
        resp     = jsonResp
    except ValueError, KeyError:
      #prepare error JSON response
      resp = 'ValueError, KeyError '+strRequestJson 
    self.response.out.write(resp)
  
  def get(self):
    #generate test form
    #TODO: delete this in release build
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    path = os.path.join(rootPath, "html/testclientgig.html")
    self.response.out.write(template.render(path, {}))
  
  def validateRecentGigId(self, recentGigId):
    recentClientGig = None
    if recentGigId and len(recentGigId) > 0:
        recentClientGig = Gig.get_by_id(int(recentGigId))
    return recentClientGig;
  
  def buildResponseJson(self, gigs, city, isdelete):
    latestGigId = ''
    if gigs and len(gigs) > 0:
      latestGigId = gigs[0].key().id()
    jsonResp = json.dumps(
                          {
                           'city'         : city,
                           'recentGigId'  : latestGigId,
                           'gigs'         : gigs,
                           'isdelete'     : isdelete
                           },
                           cls=GigJsonEncoder
                          )
    return jsonResp
  
  def getGigsJson(self, gigs):
    return json.dumps(gigs, cls=GigJsonEncoder)
  
  def getDeltaGigsByTimestamp(self, city, gigTimestamp):
    gig_query = Gig.all()
    gig_query.filter('city =', city)
    gig_query.filter('timestamp >', gigTimestamp)
    gig_query.order('-timestamp')
    #TODO: return all results in db
    tempGigs = gig_query.fetch(ClientGetGigs.CLIENT_GET_LIMIT)
    currDateTime = datetime.now()
    #can't do in one query as only one inequality filter allow in query
    #no need to convert as will be saved in db in utc format only
    gigs = [g for g in tempGigs if g.timing > currDateTime]
    return gigs
    
  def getGigs(self, city, validRecentGig):
    #get recent gig stored at client
    gig_query = Gig.all()
    gigs  = None
    if validRecentGig:
      #get all gigs gr8r than this gig's timestamp and for passed city
      #also timing > than current time
      gigs = self.getDeltaGigsByTimestamp(validRecentGig.city, 
                                          validRecentGig.timestamp)
    else:
      #probably new update, get all gigs corresponding to city
      gig_query.filter('city =', city)
      #gig_query.filter('timing >', datetime.now())
      #gig_query.order('timing')
      gig_query.order('-timestamp')
      #TODO: return all results from db
      tempGigs = gig_query.fetch(ClientGetGigs.CLIENT_GET_LIMIT)
      currDateTime = datetime.now()
      gigs = [g for g in tempGigs if g.timing > currDateTime]
    return gigs
    
      
class ClientSubmitFeedback(webapp.RequestHandler):
  
  def post(self):
    
    reqUniqueId = cgi.escape(self.request.get('unique_id')).strip()
    reqFeedback = cgi.escape(self.request.get('feedback')).strip()
    
    if reqUniqueId and reqFeedback \
      and len(reqUniqueId) > 0 and len(reqFeedback) > 0:
      #save feedback in db
      feedback  = Feedback(feedback=reqFeedback, uniqueId=reqUniqueId)
      feedback.put()
      logging.info("ClientSubmitFeedback: feedback save success")
      jsonResp = json.dumps(
                            {
                             'result'     : 'success',
                             'unique_id'  : reqUniqueId
                             }
                            )
    else:
      #invalid request received
      logging.error("ClientSubmitFeedback: feedback save failed, invalid request")
      jsonResp = json.dumps(
                            {
                             'result'     : 'fail',
                             'reason'     : 'invalid request'
                             }
                            )
    self.response.out.write(jsonResp)
      
  def get(self):
    #print the feedback form page
    logging.debug("feedback:get")
    user      = users.get_current_user()
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user:
      #print the feedback form for current user
      template_values = {
         'username' : user.nickname()
      }
      path = os.path.join(rootPath, "html/feedback.html")
    else:
      #not allowed access render error page
      template_values = {
         'errorMsg' : "Not valid user"
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))
      
    
    
    
    