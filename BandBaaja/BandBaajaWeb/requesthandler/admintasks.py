import os
import cgi
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from scenedb.gig import Gig
from scenedb.feedback import Feedback
from google.appengine.ext import db
from datetime import datetime
from urlparse import urlparse

class AdminPage(webapp.RequestHandler):
  def get(self):
    user      = users.get_current_user()
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #current user is admin render the admin homepage
      template_values = {
         'username' : user.nickname()
      }
      path = os.path.join(rootPath, "html/new_gig.html")
    else:
      #not allowed access render error page
      template_values = {
         'errorMsg' : "Not sufficient privileges."
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))
    
class FindPage(webapp.RequestHandler):
  PAGESIZE = 20
  def get(self):
    user      = users.get_current_user();
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #current user is admin render the admin homepage
      template_values = {
         'username' : user.nickname()
      }
      path = os.path.join(rootPath, "html/find.html")
    else:
      #not allowed access render error page
      template_values = {
         'errorMsg' : "Not sufficient privileges."
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))
    
  def post(self):
    user      = users.get_current_user();
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #current user is admin
      #TODO: value error if Xscripting
      goToNext            = self.request.get('goToNext');
      goToPrevious        = self.request.get('goToPrevious');
      try:
        prevLastResultIndex = int(self.request.get('lastResultIndex'))
      except ValueError:
        prevLastResultIndex = 0
      
      reqArtist   = self.request.get('artist').strip()
      reqVenue    = self.request.get('venue').strip()
      reqCity_loc = self.request.get('city_loc').strip()
      reqCity     = self.request.get('city').strip().lower()
      reqGenre    = self.request.get('genre').strip()
      reqTiming   = self.request.get('date_time').strip().lower()
      
      next = False
      prev = False
      calcOffset = 0
      
      if goToNext and len(goToNext) > 0:
        #show next PAGESIZE results
        calcOffset = prevLastResultIndex
      elif goToPrevious and len(goToPrevious) > 0:
        #show previous PAGESIZE results
        if (prevLastResultIndex % ViewGigs.PAGESIZE) != 0:
          prevLastResultIndex = (prevLastResultIndex / ViewGigs.PAGESIZE) * ViewGigs.PAGESIZE
          calcOffset = prevLastResultIndex - ViewGigs.PAGESIZE;
        else:
          calcOffset = prevLastResultIndex - 2*ViewGigs.PAGESIZE;
        calcOffset = 0 if (calcOffset < 0) else calcOffset
      else:
        calcOffset = 0
        
      gigs_query  = Gig.all()
      
      
      if reqTiming and len(reqTiming) > 0:
        #validate date time string
        timing = self.checkDateTime(reqTiming)
        gigs_query.filter('timing =', timing)
        gigs_query.order('-timing')
        
      if reqArtist and len(reqArtist) > 0:
        gigs_query.filter('artist >=', reqArtist)
        gigs_query.filter('artist <', reqArtist+u'\ufffd')
        gigs_query.order('artist')
        
      if reqVenue and len(reqVenue) > 0:
        gigs_query.filter('venue >=', reqVenue)
        gigs_query.filter('venue <', reqVenue+u'\ufffd')
        gigs_query.order('venue')
        
      if reqCity_loc and len(reqCity_loc) > 0:
        gigs_query.filter('city_loc >=', reqCity_loc)
        gigs_query.filter('city_loc <', reqCity_loc+u'\ufffd')
        gigs_query.order('city_loc')
        
      if reqCity and len(reqCity) > 0:
        gigs_query.filter('city >=', reqCity)
        gigs_query.filter('city <', reqCity+u'\ufffd')
        gigs_query.order('city')
        
      if reqGenre and len(reqGenre) > 0:
        gigs_query.filter('genre >=', reqGenre)
        gigs_query.filter('genre <', reqGenre+ u'\ufffd')
        gigs_query.order('genre')
        
      gigs_query.order('-timing')
      
      gigs = gigs_query.fetch(limit=FindPage.PAGESIZE+1, offset=calcOffset)
      
      if len(gigs) == ViewGigs.PAGESIZE + 1:
        gigs = gigs[:ViewGigs.PAGESIZE]
        next = True
      else:
        next = False
      
      
      if calcOffset == 0:
        prev = False
      else:
        prev = True
      
      lastResultIndex = calcOffset + len(gigs)
      
      template_values = {
         'arist'                : reqArtist,
         'venue'                : reqVenue,
         'city_loc'             : reqCity_loc,
         'city'                 : reqCity,
         'genre'                : reqGenre,
         'date_time'            : reqTiming,
         "gigslen"              : len(gigs),
         "gigs"                 : gigs,
         'username'             : user.nickname(),
         'lastResultIndex'      : lastResultIndex,
         'next'                 : next,
         'previous'             : prev
      }
      path = os.path.join(rootPath, "html/find_results.html")
    else:
      #not allowed access render error page
      template_values = {
         'errorMsg' : "Not sufficient privileges."
      }
      path = os.path.join(rootPath, "html/find_results.html")
    self.response.out.write(template.render(path, template_values))
    
  #check whether valid date time string and return it in datetime.datetime
  def checkDateTime(self, timing):
    try:
      dt = datetime.strptime(timing, "%m/%d/%Y %H:%M")
      return dt
    except ValueError:
      return None

    
class EditGig(webapp.RequestHandler):
  
  def get(self):
    user      = users.get_current_user()
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    successMsg = ''
    
    if user and users.is_current_user_admin():
      #user is valid
      #get the gig id and enumerate edit form
      reqGigId    = cgi.escape(self.request.get('gig_id')).strip()
      if reqGigId and len(reqGigId) > 0:
        #try to fetch gig
        gigToEdit = Gig.get_by_id(int(reqGigId))
        if gigToEdit:
          #valid gigId, now send it to edit template 
          successMsg = "valid gig being edited"
        else:
          #not found gig in db corresponding to gigid
          errorMsg = "Gig not found in DB"
      else:
        #gigid not received
        errorMsg = "empty gig id received"
    else:
      #not valid operation
      errorMsg = "Insufficient privilege"
      
    
    if len(successMsg) > 0:
      template_values = {
          'gigToEdit'      : gigToEdit,
          'username' : user.nickname()
      }
      path = os.path.join(rootPath, "html/new_gig.html");
    else:
      template_values = {
          'errorMsg'      :  errorMsg 
      }
      path = os.path.join(rootPath, "html/error.html");
      
    self.response.out.write(template.render(path, template_values))
      
  def post(self):
    user      = users.get_current_user()
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    successMsg = ''
    
    if user and users.is_current_user_admin():
      #validate form inputs an create a new gig and store it
      #if invalid error with errorMsg "add all required fields"
      #TODO: replace this as to proper init in construct
      reqArtist       = cgi.escape(self.request.get('artist')).strip()
      reqVenue        = cgi.escape(self.request.get('venue')).strip()
      reqCity_loc     = cgi.escape(self.request.get('city_loc')).strip()
      reqCity         = cgi.escape(self.request.get('city')).strip().lower()
      reqEvent_url    = cgi.escape(self.request.get('gig_url')).strip()
      reqGenre        = cgi.escape(self.request.get('genre')).strip()
      reqDescription  = cgi.escape(self.request.get('description')).strip()
      reqTiming       = cgi.escape(self.request.get('date_time')).strip().lower()
      reqLatlong      = cgi.escape(self.request.get('latlong')).strip().lower()
      requiredList    = [
                          reqArtist,
                          reqVenue,
                          reqCity_loc,
                          reqCity, 
                          reqTiming
                          ]
      
      #get the gig id to be edited
      reqGigId    = cgi.escape(self.request.get('gig_id')).strip()
      
      if not self.checkEmptyArgs(requiredList) or not self.checkDateTime(reqTiming):
        #erroneous input
        errorMsg  = "invalid request params"
      else:
        #correct input, enter these details in db
        if reqGigId and len(reqGigId) > 0:
          #try to fetch gig
          gigToEdit = Gig.get_by_id(int(reqGigId))
          if gigToEdit:
            #valid gigId, now send it to edit template 
            successMsg = "valid gig being edited"
            gigToEdit.artist  = reqArtist
            gigToEdit.venue   = reqVenue
            gigToEdit.city_loc = reqCity_loc
            gigToEdit.city    = reqCity
            gigToEdit.timing = self.checkDateTime(reqTiming)
            if len(reqEvent_url) > 0:
              url = urlparse(reqEvent_url, scheme='http')
              gigToEdit.event_url   = db.Link(url.geturl())
            gigToEdit.genre       = reqGenre
            gigToEdit.description = reqDescription
            gigToEdit.latlong     = reqLatlong
            gigToEdit.put()
            successMsg = "Event edited in db"
          else:
            #not found gig in db corresponding to gigid
            errorMsg = "Gig not found in DB"
        else:
          #gigid not received
          errorMsg = "empty gig id received"
    else:
      #not sufficient privileges
      errorMsg  = "Not sufficient privileges"
    
    if len(successMsg)>0:
      template_values = {
         'successMsg'    :  successMsg
      }
      path  = os.path.join(rootPath, "html/new_gig.html");
    else:
      template_values = {
         'errorMsg'      :  errorMsg 
      }
      path = os.path.join(rootPath, "html/error.html")
    
    self.response.out.write(template.render(path, template_values))
    
  #check whether valid date time string and return it in datetime.datetime
  def checkDateTime(self, timing):
    try:
      dt = datetime.strptime(timing, "%m/%d/%Y %H:%M")
      return dt
    except ValueError:
      return None
  
  def checkEmptyArgs(self, requiredArgs):
    for var in requiredArgs:
      if not (var and len(var) > 0):
        #invalid input
        return False
    return True

class NewGig(webapp.RequestHandler):
  
  def post(self):
    user      = users.get_current_user();
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    successMsg = ''
    if user and users.is_current_user_admin():
      #validate form inputs an create a new gig and store it
      #if invalid error with errorMsg "add all required fields"
      #TODO: replace this as to proper init in construct
      reqArtist       = cgi.escape(self.request.get('artist')).strip()
      reqVenue        = cgi.escape(self.request.get('venue')).strip()
      reqCity_loc     = cgi.escape(self.request.get('city_loc')).strip()
      reqCity         = cgi.escape(self.request.get('city')).strip().lower()
      reqEvent_url    = cgi.escape(self.request.get('gig_url')).strip()
      reqGenre        = cgi.escape(self.request.get('genre')).strip()
      reqDescription  = cgi.escape(self.request.get('description')).strip()
      reqTiming       = cgi.escape(self.request.get('date_time')).strip().lower()
      reqLatlong      = cgi.escape(self.request.get('latlong')).strip().lower()
      requiredList    = [
                          reqArtist,
                          reqVenue,
                          reqCity_loc,
                          reqCity, 
                          reqTiming
                          ]
      
      if not self.checkEmptyArgs(requiredList) or not self.checkDateTime(reqTiming):
        #erroneous input
        errorMsg  = "invalid request params"
      else:
        #correct input, enter these details in db
        gig             = Gig(artist=reqArtist, venue=reqVenue, 
                              city_loc=reqCity_loc, city=reqCity, 
                              timing=self.checkDateTime(reqTiming))
        if len(reqEvent_url) > 0:
          url = urlparse(reqEvent_url, scheme='http')
          gig.event_url   = db.Link(url.geturl())
        gig.genre       = reqGenre
        gig.description = reqDescription
        gig.latlong     = reqLatlong
        gig.put()
        successMsg = "Event entered in db"
    else:
      #not sufficient privileges
      errorMsg  = "Not sufficient privileges"
    
    if len(successMsg)>0:
      template_values = {
         'successMsg'    :  successMsg
      }
      path  = os.path.join(rootPath, "html/new_gig.html");
    else:
      template_values = {
         'errorMsg'      :  errorMsg 
      }
      path = os.path.join(rootPath, "html/error.html")
    
    self.response.out.write(template.render(path, template_values))  
      
  def checkEmptyArgs(self, requiredArgs):
    for var in requiredArgs:
      if not (var and len(var) > 0):
        #invalid input
        return False
    return True
  
  #check whether valid date time string and return it in datetime.datetime
  def checkDateTime(self, timing):
    try:
      dt = datetime.strptime(timing, "%m/%d/%Y %H:%M")
      return dt
    except ValueError:
      return None
  
class DeleteGigs(webapp.RequestHandler):
  PAGESIZE = 20
  def post(self):
    user      = users.get_current_user();
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #delete the specified gigs from db
      prev = False
      next = True
      gigIds = self.request.get_all('gig_id');
      deletedGigs = 0
      
      for id in gigIds:
        gig = Gig.get_by_id(int(id))
        if gig:
          gig.delete()
          deletedGigs += 1
      
      gigs_query  = Gig.all().order('-timing')
      gigs        = gigs_query.fetch(DeleteGigs.PAGESIZE+1)

      if len(gigs) == DeleteGigs.PAGESIZE+1:
        gigs = gigs[:DeleteGigs.PAGESIZE]
        next = True
      
      template_values = {
        "gigslen"  : len(gigs),
        "gigs"     : gigs,
        'username' : user.nickname(),
        'success_msg' : str(deletedGigs)+" gigs successfully deleted",
        'next'      : next,
        'previous'         : prev,
        'lastResultIndex'  : len(gigs),
      }
      path = os.path.join(rootPath, "html/view.html")
    else:
      #insufficient privileges
      template_values = {
        'error_msg' : "insufficient privileges"
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))

class ViewGigs(webapp.RequestHandler):
  PAGESIZE = 20
  def get(self):
    user      = users.get_current_user();
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #TODO: value error if Xscripting
      goToNext            = self.request.get('goToNext');
      goToPrevious        = self.request.get('goToPrevious');
      try:
        prevLastResultIndex = int(self.request.get('lastResultIndex'))
      except ValueError:
        prevLastResultIndex = 0

      next = False
      prev = False
      calcOffset = 0
      
      gigs_query  = Gig.all().order('-timing')
      
            
      if goToNext and len(goToNext) > 0:
        #show next PAGESIZE results
        calcOffset = prevLastResultIndex
      elif goToPrevious and len(goToPrevious) > 0:
        #show previous PAGESIZE results
        if (prevLastResultIndex % ViewGigs.PAGESIZE) != 0:
          prevLastResultIndex = (prevLastResultIndex / ViewGigs.PAGESIZE) * ViewGigs.PAGESIZE
          calcOffset = prevLastResultIndex - ViewGigs.PAGESIZE;
        else:
          calcOffset = prevLastResultIndex - 2*ViewGigs.PAGESIZE;
        calcOffset = 0 if (calcOffset < 0) else calcOffset
      else:
        calcOffset = 0
        
      gigs = gigs_query.fetch(limit=ViewGigs.PAGESIZE+1, offset=calcOffset)
      
      if len(gigs) == ViewGigs.PAGESIZE + 1:
        gigs = gigs[:ViewGigs.PAGESIZE]
        next = True
      else:
        next = False
      
      if calcOffset == 0:
        prev = False
      else:
        prev = True
      
      lastResultIndex = calcOffset + len(gigs)
      
      template_values = {
         "gigslen"              : len(gigs),
         "gigs"                 : gigs,
         'username'             : user.nickname(),
         'lastResultIndex'      : lastResultIndex,
         'next'                 : next,
         'previous'             : prev
      }
      path = os.path.join(rootPath, "html/view.html")
    else:
      template_values = {
        'errorMsg' : 'Not sufficient privileges' 
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))
  
  #check whether valid date time string and return it in datetime.datetime
  def checkDateTime(self, timing):
    try:
      dt = datetime.strptime(timing, "%m/%d/%Y %H:%M")
      return dt
    except ValueError:
      return None    
    

class ViewFeedback(webapp.RequestHandler):
  
  PAGESIZE  = 5
  
  def get(self):
    user      = users.get_current_user();
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #TODO: value error if Xscripting
      goToNext            = self.request.get('goToNext');
      goToPrevious        = self.request.get('goToPrevious');
      try:
        prevLastResultIndex = int(self.request.get('lastResultIndex'))
      except ValueError:
        prevLastResultIndex = 0
      
      next = False
      prev = False
      calcOffset = 0
      
      feedback_query = Feedback.all().order('-timestamp')
      
      if goToNext and len(goToNext) > 0:
        #show next PAGESIZE results
        calcOffset = prevLastResultIndex
      elif goToPrevious and len(goToPrevious) > 0:
        #show previous PAGESIZE results
        if (prevLastResultIndex % ViewFeedback.PAGESIZE) != 0:
          prevLastResultIndex = (prevLastResultIndex / ViewFeedback.PAGESIZE) * ViewFeedback.PAGESIZE
          calcOffset = prevLastResultIndex - ViewFeedback.PAGESIZE;
        else:
          calcOffset = prevLastResultIndex - 2*ViewFeedback.PAGESIZE;
        calcOffset = 0 if (calcOffset < 0) else calcOffset
      else:
        calcOffset = 0
      
      feedbacks = feedback_query.fetch(limit=ViewFeedback.PAGESIZE+1, offset=calcOffset)
      
      if len(feedbacks) == ViewFeedback.PAGESIZE + 1:
        feedbacks = feedbacks[:ViewFeedback.PAGESIZE]
        next = True
      else:
        next = False
      
      if calcOffset == 0:
        prev = False
      else:
        prev = True
      
      lastResultIndex = calcOffset + len(feedbacks)
      
      template_values = {
         "feedbackslen"         : len(feedbacks),
         "feedbacks"            : feedbacks,
         'username'             : user.nickname(),
         'lastResultIndex'      : lastResultIndex,
         'next'                 : next,
         'previous'             : prev
      }
      path = os.path.join(rootPath, "html/view_feedback.html")
    else:
      template_values = {
        'errorMsg' : 'Not sufficient privileges' 
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))
  
      
class DeleteFeedbacks(webapp.RequestHandler):
  PAGESIZE = 5
  def post(self):
    user      = users.get_current_user();
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #delete the specified feedbacks from db
      prev = False
      next = True
      FeedbackIds = self.request.get_all('feedback_id');
      deletedFeedbacks = 0
      
      for id in FeedbackIds:
        feedback = Feedback.get_by_id(int(id))
        if feedback:
          feedback.delete()
          deletedFeedbacks += 1
      
      feedbacks_query  = Feedback.all().order('-timestamp')
      feedbacks        = feedbacks_query.fetch(DeleteFeedbacks.PAGESIZE+1)

      if len(feedbacks) == DeleteFeedbacks.PAGESIZE+1:
        feedbacks = feedbacks[:DeleteFeedbacks.PAGESIZE]
        next = True
      
      template_values = {
        "feedbackslen"  : len(feedbacks),
        "feedbacks"     : feedbacks,
        'username' : user.nickname(),
        'success_msg' : str(deletedFeedbacks)+" feedbacks successfully deleted",
        'next'      : next,
        'previous'         : prev,
        'lastResultIndex'  : len(feedbacks),
      }
      path = os.path.join(rootPath, "html/view_feedback.html")
    else:
      #insufficient privileges
      template_values = {
        'error_msg' : "insufficient privileges"
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))
      

class FacebookRetrieve(webapp.RequestHandler):
  def get(self):
    user      = users.get_current_user()
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #current user is admin render the admin homepage
      template_values = {
         'username' : user.nickname()
      }
      path = os.path.join(rootPath, "html/fbauto.html")
    else:
      #not allowed access render error page
      template_values = {
         'errorMsg' : "Not sufficient privileges."
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))

class FacebookAdd(webapp.RequestHandler):
  def get(self):
    user      = users.get_current_user()
    rootPath  = os.path.split(os.path.dirname(__file__))[0]
    if user and users.is_current_user_admin():
      #current user is admin render the admin homepage
      template_values = {
         'username' : user.nickname()
      }
      path = os.path.join(rootPath, "html/insert_gig.html")
    else:
      #not allowed access render error page
      template_values = {
         'errorMsg' : "Not sufficient privileges."
      }
      path = os.path.join(rootPath, "html/error.html")
    self.response.out.write(template.render(path, template_values))

