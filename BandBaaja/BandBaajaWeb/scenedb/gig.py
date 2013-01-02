
from google.appengine.ext import db;

class Gig(db.Model):
  artist      = db.StringProperty(required=True)
  venue       = db.StringProperty(required=True)
  city_loc    = db.StringProperty(required=True)
  
  city        = db.StringProperty(required=True, choices=set(["delhi", "bangalore", "mumbai", "others"]))
  latlong     = db.StringProperty()
  event_url   = db.LinkProperty()
  genre       = db.StringProperty()
  description = db.TextProperty()
  bookmarked  = db.BooleanProperty(default=False)
  timing      = db.DateTimeProperty(required=True)
  timestamp   = db.DateTimeProperty(auto_now_add=True)
  