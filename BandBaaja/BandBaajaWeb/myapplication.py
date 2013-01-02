from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

from requesthandler.mainpage import MainPage
from requesthandler.admintasks import AdminPage
from requesthandler.admintasks import ViewGigs
from requesthandler.admintasks import ViewFeedback
from requesthandler.admintasks import DeleteFeedbacks
from requesthandler.admintasks import NewGig
from requesthandler.admintasks import EditGig
from requesthandler.admintasks import DeleteGigs
from requesthandler.admintasks import FindPage
from requesthandler.admintasks import FacebookRetrieve
from requesthandler.admintasks import FacebookAdd
from requesthandler.clienttasks import ClientGetGigs
from requesthandler.clienttasks import ClientSubmitFeedback

import logging

application = webapp.WSGIApplication(
                                     [('/admin/login', MainPage),
                                      ('/admin/adminpage', AdminPage),
                                      ('/admin/view', ViewGigs),
                                      ('/new/gig', NewGig),
                                      ('/edit/gig', EditGig),
                                      ('/delete/gigs', DeleteGigs),
                                      ('/admin/find', FindPage),
                                      ('/get/gigs', ClientGetGigs),
                                      ('/feedback', ClientSubmitFeedback),
                                      ('/admin/viewfeedback', ViewFeedback),
                                      ('/delete/feedbacks', DeleteFeedbacks),
                                      ('/admin/facebookRetrieve', FacebookRetrieve),
                                      ('/admin/facebookAdd', FacebookAdd)
                                      
                                      ],
                                     debug=True
                                     )

def main():
  #set the logging level
  logging.getLogger().setLevel(logging.DEBUG)
  run_wsgi_app(application)
  
if __name__ == "main":
  main()