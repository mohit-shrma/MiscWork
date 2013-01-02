/*
 * callback - function which will get response to render html
 */

var FbGetEvents = function() {
    
    var currSince 	= "";
    var currUntil 	= "";
    var session 	= null;
    
    var FbDoLogin = function(callback) {
    
        var permissions = {scope:'read_stream'};
        
        var loginCallback = function(response) {
            
            if (response.authResponse) {
                //logged in
            	session = response.authResponse;
            	callback(true);
            } else {
                //not logged in
                callback(false);
            }
        };
        
        var performLogin = function() {
          FB.login(loginCallback, permissions);  
        };
            
        performLogin();    
    };
    
    var GetStatusLikeWatScene = function(url, callback) {
    	
    	var fetchedSince = "";
        var fetchedUntil = "";
        var fetchedStatuses = [];
        
        var parseResponseForStatuses = function(response) {
        	
        	var respData = response.data;
        	var pagingData = response.paging;
        	
        	//get statuses containing "::"
        	if (respData !== null) {
        		for (var i=0; i < respData.length; i++) {
        			if (respData[i].type == "status"
        				&& respData[i].message.indexOf("::") != -1) {
        					//push this status id
        					fetchedStatuses.push(respData[i].id);
        			}
        		}
        	}
        	
        	if (pagingData !== null && pagingData !== undefined) {
               
               var ind = null;

               if (pagingData.previous !== null && pagingData.previous !== undefined) {
                    ind = pagingData.previous.indexOf("since=")+"since=".length;
                    fetchedSince = parseInt(pagingData.previous.substr(ind), 10);                    
               }
                              
               if (pagingData.next !== null && pagingData.next !== undefined) {
                   ind = pagingData.next.indexOf("until=")+"until=".length;
                   fetchedUntil = parseInt(pagingData.next.substr(ind), 10);
               }
           }
           
           SendToCallback();
        };
        
        var GetResponse = function() {
          //TODO: "since" and "until" if passed
          FB.api(url, parseResponseForStatuses);  
        };
        
        var SendToCallback = function() {
          
          var ret = {
            statuses : fetchedStatuses,
            since  : fetchedSince,
            until  : fetchedUntil
          };  
          
          callback(ret);
        };
 		
 		GetResponse();       
    };
	
	/*
	 * for links in format 
	 * http://www.facebook.com/events/294851007214314/
	 */
	var GetEventIdFromLink = function(strLink) {
	    var eventId = null;
	    if (strLink !== null && strLink.length > 0) {
	    	if (strLink.indexOf("event.php") > 0) {
				eventId = strLink.substring(strLink.indexOf('=')+1);
				eventId = parseInt(eventId, 10)+'';
	    	} else {
	    		var endSep = strLink.lastIndexOf("/");
		        var startSep = strLink.lastIndexOf("/", endSep-1);
		        if (startSep > 0 && endSep > startSep) {
		            eventId = strLink.substring(startSep+1, endSep);	            
		        }	
	    	}
	    }
	    return eventId;
	};
	
	var ParseResponseDataForEvents = function(respData) {
		
		var fetchedEvents = [];
		
		var tempLink = "";
        var tempEventId = "";
        var tempComments = {};
		
		//get events data
		if (respData !== null) {
	       for (var i = 0; i < respData.length; i++) {
               
             if (respData[i].type == "link" && respData[i].link !== null 
                    && respData[i].link !== undefined ) {
                    
                    tempLink = respData[i].link;
                    
                 if (tempLink.indexOf("facebook.com/events/") > 0 
                 		|| tempLink.indexOf("event.php") > 0) {
                     
                     //event link found
                     
                     //extract event id
                     tempEventId = GetEventIdFromLink(tempLink);
                     
                     if (tempEventId !== null) {
                        fetchedEvents.push(tempEventId);
                     }
                 }
             }
             
             if (respData[i].comments !== undefined && respData[i].comments !== null
                    && respData[i].comments.data !== undefined && 
                    respData[i].comments.data !== null) {
                
                tempComments = respData[i].comments.data;         
                
                for (var j = 0; j <  tempComments.length; j++) {
                
                    tempLink = tempComments[j].message.trim();
                
                    if (tempLink.indexOf("facebook.com/events/") > 0
                            || tempLink.indexOf("event.php") > 0) {
                     
                         //event link found
                         
                         //extract event id
                         tempEventId = GetEventIdFromLink(tempLink);
                     
                         if (tempEventId !== null) {    
                            fetchedEvents.push(tempEventId);
                         }
                    }
                }
             }
           }
       }
		
		return fetchedEvents;
	};
	
	    
    var FbGetAndParseResponse = function(url, callback) {
    
        var fetchedEvents = [];
        var fetchedSince = "";
        var fetchedUntil = "";
        
        var ParseResponse = function(response) {
           
           
           var respData = response.data;
           var tempLink = "";
           var tempEventId = "";
           var tempComments = {};
           
           var pagingData = response.paging;
           
           //get events data
           fetchedEvents = ParseResponseDataForEvents(respData);
           
           if (pagingData !== null && pagingData !== undefined) {
               
               var ind = null;

               if (pagingData.previous !== null && pagingData.previous !== undefined) {
                    ind = pagingData.previous.indexOf("since=")+"since=".length;
                    fetchedSince = parseInt(pagingData.previous.substr(ind), 10);                    
               }
                              
               if (pagingData.next !== null && pagingData.next !== undefined) {
                   ind = pagingData.next.indexOf("until=")+"until=".length;
                   fetchedUntil = parseInt(pagingData.next.substr(ind), 10);
               }
           }
          SendToCallback(); 
        };
        
        var GetResponse = function() {
          //TODO: "since" and "until" if passed
          FB.api(url, ParseResponse);  
        };
        
        var SendToCallback = function() {
          
          var ret = {
            events : fetchedEvents,
            since  : fetchedSince,
            until  : fetchedUntil
          };  
          
          callback(ret);
        };
        
        GetResponse();    
    };
    
    var FbGetStatusesDetail = function (statuses, callback) {
    	
      var detailedStatuses = {};
	  
	  var ParseResponse  = function (response) {
          detailedStatuses = response;
          sendToCallback();
      };
      
      var GetResponse = function() {
          var url = '?ids='+statuses.join(',') 
                     + '&date_format=U';
          FB.api(url, ParseResponse);
      };
        
      var sendToCallback = function() {
        callback(detailedStatuses);  
      };
        
      GetResponse();
    };
    
    var FbGetEventsDetail = function(events, callback) {
  
      var DetailedEvents = {};
      
      var ParseResponse  = function (response) {
          DetailedEvents = response;
          sendToCallback();
      };
      
      var GetResponse = function() {
          var url = '?ids='+events.join(',') 
                     + '&fields=name,start_time,location,description,venue'
                     + '&date_format=U';
          FB.api(url, ParseResponse);
      };
        
      var sendToCallback = function() {
        callback(DetailedEvents);  
      };
        
      GetResponse();
    };
    
    
    var FbGetEvents = function(url, callback) {
        FbGetAndParseResponse(url, function(response){
            currSince = response.since;
            currUntil = response.until;
            if (response.events.length > 0) {
                FbGetEventsDetail(response.events, function(detailedEvents){
                   callback(prepCallbackResponse(detailedEvents)); 
                });                
            } else {
                callback(prepCallbackResponse({}));
            }
        });
    };
    
    var GetEventsLikeWatScene = function(url, callback) {
		
		//get status Ids like watscene
		GetStatusLikeWatScene(url, function(response) {
			
			currSince = response.since;
            currUntil = response.until;
            
            if (response.statuses.length > 0) {
				//fetch statuses corresponding to Ids
        		FbGetStatusesDetail(response.statuses, function(detailedStatusesDict){
        			
        			//convert this dic to an array
        			arrDetailedStatuses = [];
        			for (key in detailedStatusesDict) {
        			    arrDetailedStatuses.push(detailedStatusesDict[key]);
        			}
        			
        			//parse these fetched statuses for events
        			var fetchedEvents = ParseResponseDataForEvents(arrDetailedStatuses);
        			
        			//fetch event details
        			if (fetchedEvents.length > 0) {
	        			FbGetEventsDetail(fetchedEvents, function(detailedEvents){
	                   		callback(prepCallbackResponse(detailedEvents)); 
	                	});                
	            	} else {
	                	callback(prepCallbackResponse({}));
            		}
        		});
				
            } else {
            	callback(prepCallbackResponse({}));
            }
		});
		
		
    };
    
    var HandleLogin = function (bResp) {
      
      if (bResp) {
        //logged in
        //TODO: prep URL
        var url = "";
        FbGetAndParseResponse(url, HandleEventsResponse);
      } else {
        //logged out
        var obj = prepCallbackResponse({});
        obj.error = "not logged in";
        callback(obj);   
      }  
    };
    
    var HandleEventsResponse = function(response) {
        
        currSince = response.since;
        currUntil = response.until;
        
        if (response.events.length > 0) {
           FbGetEventsDetail(response.events, HandleEventDetails);             
        } else {
           callback(prepCallbackResponse({}));    
        }
    };
    
    var prepCallbackResponse = function(eventsResp) {
        return {
            events : eventsResp,
            since  : currSince,
            until  : currUntil
        };
    };
    
    var HandleEventDetails = function(response) {
        callback(prepCallbackResponse(response));
    };
    
    return {
        
        setCallback : function(passedCallback) {
            callback = passedCallback;    
        },
        
        doLogin     :   function(loginCallback){
             FbDoLogin(loginCallback);
        },
        
        fetchEvents : function(url, callback) {
            FbGetEvents(url, callback);
        },
        
        fetchEventsDetail : function(eventIds, callback) {
        	FbGetEventsDetail(eventIds, callback);
        },
        
        fetchEventsLikeWatScene : function(url, callback) {
        	GetEventsLikeWatScene(url, callback);
        }
    };
};





