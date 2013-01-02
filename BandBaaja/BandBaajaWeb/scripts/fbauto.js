var fbAuto = (function() {
	
	var fbGetEvents = FbGetEvents();
	
	var currEvents = null;
	
	//initialize to login url
	var urlPrefix = "/me/home";
	
	var getUrlPrefix = function() {
		return urlPrefix;
	};
	
	var setUrlPrefix = function(newUrlPrefix) {
		urlPrefix = newUrlPrefix;
	};
	
	var appendEvents = function(eventsDict) {
	    for (key in eventsDict) {
	        $('#eventsDiv').append(getEventHTML(eventsDict[key]));   
	    }
	};
	
	var clearEvents = function() {
		$('#eventsDiv').html('');
	};
	
	//diff of 7 hrs coming
	var convToIndianTime = function(epochSecs) {
		var indianSecs = epochSecs-(7*60*60);
		var indianMs = indianSecs*1000;
		var inDate = new Date(indianMs);
		return ""+(inDate.getUTCMonth()+1)+"/"+inDate.getUTCDate()+"/"+inDate.getUTCFullYear()+" "
				+inDate.getUTCHours()+":"+inDate.getUTCMinutes();
				
	};
	
	var getEventHTML = function(eventObject) {
	    
		  var htmlString = "";
		  
		  htmlString += '<div class=\"event\"><table cellspacing=\"10\"><tr><td>Name</td><td><a target=\"_blank\" href=\"https://www.facebook.com/event.php?eid=';
		  htmlString += eventObject.id;
		  htmlString += '\">';
		  htmlString += eventObject.name;
		  htmlString += '</a></td><td><a href="/admin/facebookAdd" onclick="fbAuto.fbAddEventToLocalStore(\''+eventObject.id+'\')" target="_blank">Add</a></td></tr><tr><td>Location</td><td>';
		  htmlString += eventObject.location;
		  htmlString += '</td></tr><tr><td>Time</td><td>';
		  htmlString += convToIndianTime(eventObject.start_time);
		  htmlString += '</td></tr></table><details><summary>Description</summary><p>';
		  htmlString += eventObject.description;
		  htmlString += '</p></details></div>';
		  
		  return htmlString;
	 };      
	 
	 var onFetchEventsDetail = function(detailedEvents) {
		 if (detailedEvents !== null) {
			 appendEvents(detailedEvents);
			 currEvents = detailedEvents;
		 }
	 };
	
	 var onFetchEvents = function(events) {
		  var detailedEvents = events.events;
		  var since = events.since;
		  var until = events.until;
		  fbAddSinceUntil(since, until);
		  onFetchEventsDetail(detailedEvents);
	 };
	 
	 var supports_html5_sessionStorage = (function () {
		  try {
		    return 'sessionStorage' in window && window['sessionStorage'] !== null;
		  } catch (e) {
		    return false;
		  }
		})();
	 
	 var supports_html5_localStorage = (function () {
		  try {
		    return 'localStorage' in window && window['localStorage'] !== null;
		  } catch (e) {
		    return false;
		  }
		})();
	
	 var fbAddSinceUntil = function(since, until) {
		if (supports_html5_sessionStorage) {
			sessionStorage.setItem('fbSince', since);
			sessionStorage.setItem('fbUntil', until);
		}
	 };
	
	var checkAndLocalStore = function(key, value) {
		if (value !== null && value !== undefined) {
			localStorage.setItem(key, value);
		}
	}
	
	var fbAddEventToLocalStore = function(eventId) {
	    //extract event from events detail
		if (currEvents !== null) {
			var event = currEvents[eventId];
		    if (supports_html5_localStorage)
		    {
		        //add event to session store
		    	localStorage.clear();
		    	checkAndLocalStore('fbCacheEventId', eventId);
		    	checkAndLocalStore('fbCacheEventName', event.name);
		    	checkAndLocalStore('fbCacheEventStartTime', event.start_time);
		    	checkAndLocalStore('fbCacheEventLocation', event.location);
		    	checkAndLocalStore('fbCacheEventDescription', event.description);
		        
		        //find venue
		        var preciseLocation = "";
		        var latLong = "";
		        if (event.venue !== null && event.venue !== undefined) {
		        	
		        	if (event.venue.street !== null
		        			&& event.venue.street !== undefined) {
		        		preciseLocation += event.venue.street;
		        	}
		        	
		        	if (event.venue.city !== null
		        			&& event.venue.city !== undefined) {
		        		preciseLocation += 
		        			preciseLocation.length>0 ? ", "+event.venue.city: event.venue.city;
		        	}
		        	
		        	if (event.venue.latitude !== null &&
		        		event.venue.longitude !== null &&
		        		event.venue.latitude !== undefined &&
		        		event.venue.longitude !== undefined ) {
		        		latLong = event.venue.latitude + ","
		        					+ event.venue.longitude
						
		        		
		        	}
		        	
		        }

		        checkAndLocalStore('fbCacheEventPreciseLoc', preciseLocation);
		        checkAndLocalStore('fbCacheEventLatLong', latLong);
		        
		    }
		}
	};
	
	//fetch events using watscene parsing
	var fetchEventsLikeWatScene = function (paginationParam) {
		clearEvents();
		fbGetEvents.fetchEventsLikeWatScene(urlPrefix + '?date_format=m/d/Y H:i'
													  + paginationParam,
											onFetchEvents);
	};
	
	var fetchEventsLikeWatSceneInitial = function() {
        fetchEventsLikeWatScene('');
    };
	
	var fetchWatSceneSince = function() {
        if (supports_html5_sessionStorage) {
            var since = sessionStorage.getItem('fbSince');
            if (since !== null && since !== undefined) {
                fetchEventsLikeWatScene('&since='+since);
            }
        }
    };
    
    var fetchWatSceneUntil = function() {
        if (supports_html5_sessionStorage) {
            var until = sessionStorage.getItem('fbUntil');
            if (until !== null && until !== undefined) {
                fetchEventsLikeWatScene('&until='+until);
            }
        }
    };
	
	//paginationparam+++ '&since='+since ||ly for until
	var fetchEvents = function(paginationParam) {
		clearEvents();
		fbGetEvents.fetchEvents(urlPrefix + '?date_format=m/d/Y H:i'
								+ paginationParam,
								onFetchEvents);
	};
	
	var fetchEventsSince = function() {
		if (supports_html5_sessionStorage) {
			var since = sessionStorage.getItem('fbSince');
			if (since !== null && since !== undefined) {
				fetchEvents('&since='+since);
			}
		}
	};
	
	var fetchEventsUntil = function() {
		if (supports_html5_sessionStorage) {
			var until = sessionStorage.getItem('fbUntil');
			if (until !== null && until !== undefined) {
				fetchEvents('&until='+until);
			}
		}
	};
	
	var fetchEventsInitial = function() {
		fetchEvents('');
	};
	
	var fetchEventsDetail = function(eventIds) {
		clearEvents();
		fbGetEvents.fetchEventsDetail(eventIds, onFetchEventsDetail);
	};
	
	var doLogin	= function() {
		var onLogIn = function(isLoggedIn) {
			if (isLoggedIn) {
				//logged in
				setUrlPrefix('/me/home');
				fetchEventsInitial();
			} else {
				//not logged in
				
			}
		};
		fbGetEvents.doLogin(onLogIn);
	};
	
	return {
		supports_html5_sessionStorage 	: supports_html5_sessionStorage,
		fbAddEventToLocalStore 			: fbAddEventToLocalStore,
		setUrlPrefix					:	setUrlPrefix,
		fetchEventsLikeWatSceneInitial  :   fetchEventsLikeWatSceneInitial,
		fetchWatSceneSince              :   fetchWatSceneSince,
		fetchWatSceneUntil              :   fetchWatSceneUntil,
		fetchEventsInitial				:	fetchEventsInitial,
		fetchEventsUntil				:	fetchEventsUntil,
		fetchEventsSince				:	fetchEventsSince,
		fetchEventsDetail				:	fetchEventsDetail,
		doLogin							:	doLogin
	};
	 
})();



$(document).ready(function() {
	
	FB.init({
        appId  : 'xxx',
        /*
        status : true, // check login status
        cookie : true, // enable cookies to allow the server to access the session
        xfbml  : true, // parse XFBML
        channelUrl : '', // channel.html file
        */
        oauth  : true // enable OAuth 2.0
        
      });
	
	$("#fbSince").click(function(){
		fbAuto.fetchEventsSince();
	});
	
	$("#fbUntil").click(function(){
		fbAuto.fetchEventsUntil();
	});
	
	$("#fb-login").click(function() {
		fbAuto.doLogin();
	});
	
	$("#getEvent").click(function() {
		//TODO: check if logged in
		var eventIds = $("#eventId").val();
		if (eventIds !== null && eventIds !== undefined && eventIds.length > 0) {
			eventIds = (eventIds.trim()).split(',');
			//try to fetch event corresponding to eventId
			fbAuto.fetchEventsDetail(eventIds);
		}
	});
	
	$("#parseWatScene").click(function(){
	  	//TODO: check if logged in
	  	var fbId = $("#fbId").val();
	  	if (fbId !== null && fbId !== undefined && fbId.length > 0) {
			fbId = fbId.trim();
			fbAuto.setUrlPrefix('/'+fbId+'/feed');
		    fbAuto.fetchEventsLikeWatSceneInitial();	
		}
	});
	
	$("#fbWatSceneSince").click(function(){
        fbAuto.fetchWatSceneSince();
    });
    
    $("#fbWatSceneUntil").click(function(){
        fbAuto.fetchWatSceneUntil();
    });
    
	
	$("#idParser").click(function() {
		//TODO: check if logged in
		var fbId = $("#fbId").val();
		if (fbId !== null && fbId !== undefined && fbId.length > 0) {
			fbId = fbId.trim();
			fbAuto.setUrlPrefix('/'+fbId+'/feed');
			fbAuto.fetchEventsInitial();
		}
	});
	
});




