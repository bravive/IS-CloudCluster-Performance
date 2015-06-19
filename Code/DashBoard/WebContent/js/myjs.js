function relationDiagram(userId){
	 $.get('twitter?q=11', "userId=" + userId, function(relation) {
		    var showID = $("#showID");
	        showID.html(userId)
		 	console.log(relation);
	        /*
	        var nodes = [
	             {id: 1, label: 'Node 1'},
	             {id: 2, label: 'Center'},
	             {id: 3, label: 'Node 3'},
	             {id: 4, label: 'Node 4'},
	             {id: 5, label: 'Node 5'}
	         ];
	
	         // create an array with edges
	         var edges = [
	             {from: 1, to: 2,label:1},
	             {from: 2, to: 1,label:1},
	             {from: 1, to: 3,label:2},
	             {from: 2, to: 4,label:3},
	             {from: 2, to: 5,label:4}
	         ];
	         */
	         var nodes = [];
	         var edges = [];
	         var itemNode = {"id": -1,"label":userId};
        	 nodes.push(itemNode);
	         for (var i = 0; i < relation.length; i++) {
	        	 var fUserID = relation[i]["fUserId"];
	        	 var num = relation[i]["Num"];
	        	 var rel = relation[i]["rel"];
	        	 var itemNode = {"id": i,"label":fUserID};
	        	 nodes.push(itemNode);
	        	 if (rel=="-") {
	        		 var itemEdge = {"from": -1, "to":i,"label": num};
		        	 edges.push(itemEdge);
	        	 } else if (rel=="+") {
	        		 var itemEdge = {"from": i, "to":-1,"label": num};
		        	 edges.push(itemEdge);
	        	 } else if (rel=="*"){
	        		 var itemEdge1 = {"from": i, "to":-1,"label": num};
		        	 edges.push(itemEdge1);
		        	 var itemEdge2 = {"from": -1, "to":i,"label": num};
		        	 edges.push(itemEdge2);
	        	 }
	         }
	         // create a network
	         var container = document.getElementById('relationDiagram');
	         
	         var data = {
	             nodes: nodes,
	             edges: edges
	         };
	         var options = {
	             edges:{
	                 style: 'arrow',
	                 arrowScaleFactor: 1
	             }
	         };
	         var network = new vis.Network(container, data, options);
	 });
}
function getContext(tweetId, userId,time){
	console.log(userId);
	console.log(time);
    var context = $("#context");

    //var getData = frm.serializeArray();
    //var formURL = $(this).attr("action");
    $.get('twitter?q=10', "tweetId=" +tweetId + "&" + "userId=" + userId + "&" + "time=" + time, function(data) {
    	console.log(data);
        //var userid ="User ID : "  + data.userId;
        //var time ="Time : "  + data.time;
        var rs = data.message;
        
        context.html(tweetId + " context :" + rs);
    });
}

