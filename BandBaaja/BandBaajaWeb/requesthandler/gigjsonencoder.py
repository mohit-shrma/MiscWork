import django.utils.simplejson as json 
from scenedb.gig import Gig
class GigJsonEncoder(json.JSONEncoder):
  def default(self, obj):
    if isinstance(obj, Gig):
      #return JSON encoded data
      return {
                'venue'       : obj.venue,
                'artist'      : obj.artist,
                'city_loc'    : obj.city_loc,
                'city'        : obj.city,
                'event_url'   : str(obj.event_url) if obj.event_url else '',
                'genre'       : obj.genre if obj.genre else '',
                'description' : obj.description if obj.description else '',
                'bookmarked'  : 'true' if obj.bookmarked else 'false',
                'date_time'   : obj.timing.strftime('%Y-%m-%d %H:%M'),
                'latlong'     : obj.latlong
                #'id'          : obj.key().id(),
                #'timestamp'   : obj.timestamp.strftime('%Y-%m-%d %H:%M'),
              }
    return json.JSONEncoder.default(self, obj)