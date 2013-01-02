
class C2DMQuotaExceedError(Exception):
  
  def __init__(self, errorCode):
    self.errorCode = errorCode
  
  def __str__(self):
    return repr(self.errorCode)
  
