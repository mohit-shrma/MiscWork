from google.appengine.ext import db

class Feedback(db.Model):
  feedback  = db.StringProperty(required=True)
  uniqueId  = db.StringProperty(required=True)
  timestamp = db.DateTimeProperty(auto_now_add=True)