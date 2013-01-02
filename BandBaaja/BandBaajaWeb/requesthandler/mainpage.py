from google.appengine.ext import webapp
from google.appengine.api import users


class MainPage(webapp.RequestHandler):
  def get(self):
    
    user  = users.get_current_user()
    
    
    
    if user:
      #self.response.headers['Content-Type'] = 'text/plain'
      self.response.out.write('Hello ')
      self.response.out.write(user.nickname())
      self.response.out.write(" !")
      url = users.create_logout_url(self.request.uri)
      self.response.out.write("<a href=\""+url+"\">Logout</a>")
    else:
      url = users.create_login_url(self.request.uri)
      self.redirect(url)
    
    