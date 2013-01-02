from google.appengine.ext import db

class C2dmUser(db.Model):
  deviceId    = db.StringProperty(required=True)
  regId       = db.StringProperty(required=True)
  city        = db.StringProperty(default="")
  lastModify  = db.DateTimeProperty(auto_now=True)