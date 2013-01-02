from datetime import tzinfo
from datetime import timedelta 

class GMT530(tzinfo):
  #indian GMT
  def utcoffset(self, dt):
    #51/2 hours ahead of GMT
    return timedelta(hours=5, minutes=30)
  
  def tzname(self, dt):
    return "GMT +530"
  
  def dst(self, dt):
    return timedelta(0)
  