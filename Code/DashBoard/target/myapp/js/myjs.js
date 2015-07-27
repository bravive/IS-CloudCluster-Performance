$(document).ready(function(){
	var cpuchart;
	var memrchart;
	var memwchart;
	var diochart;

	$('#myModal').on('hidden.bs.modal', function () {
  		cpuchart.load({unload: true});
  		memrchart.load({unload: true});
  		memwchart.load({unload: true});
  		diochart.load({unload: true});
  		$('[data-key="net"]').remove();
	})

 	$('#display').click( function () {

 		var tabs = $("#alltabs").tabs();
 		var ul = tabs.find("ul");
 		var selectedInsNum = oTable.rows('.selected').data().length;
 		for (var i = 0; i < selectedInsNum; i++) {
 			var tmpID = "Network-" + i;
 			$("<li data-key='net'><a href='#Network-" + i + "' data-toggle='tab'>" + tmpID + "</a></li>" ).appendTo( ul );
 			tabs.append("<div data-key='net' class='tab-pane fade' id='" + tmpID + "'><div id='" + tmpID + "Graph' style='margin-top:10px;margin-left:30px;width:500px;height:400px'></div>");
 			tabs.tabs("refresh");
 		} 		

 		//performances is used to store the return value from the server
 		var performances = [];
 		//get the selected instance in the table and make a json which will be send to server
 		var selectInstance = [];
    	for (var i = 0; i < oTable.rows('.selected').data().length; i++) {
    		//console.log(oTable.rows('.selected').data()[i].instanceDNS);
    		selectInstance.push(oTable.rows('.selected').data()[i].instanceDNS);
    	}  
    	// var selectInstanceJson1 = JSON.stringify(selectInstance);
    	// console.log(selectInstanceJson1);
    	var selectInstanceObject = new Object();
    	selectInstanceObject.selected = selectInstance;
    	//console.log(selectInstanceObject);
    	var selectInstanceJson = JSON.stringify(selectInstanceObject);
    	//console.log(selectInstanceJson); 
    	$.ajax({
    		type: 'get',
    		url: 'performance',
    		dataType: 'JSON',
    		data: {
    			selected: selectInstanceJson
    		},
    		success : function(resp) {
	 			performances = resp;
	 			console.log(performances);
	 			//console.log(performances.length);
	 			var datas = new Array();
				var xValue = new Array();
				//console.log(performances.length);
				//prefare for the x axis value, xValue and datas will be used to form the parameter of xs
				for (var i = 0; i < performances.length; i++) {
					xValue[i] = 'x' + i;
				}
				for ( i = 0; i < performances.length; i++) {
					datas[i] = performances[i].id;
				}
 				
 				//var xsPara = '{' + "\"" + datas[0] + "\": \"x1\",\""+datas[1]+"\": \"x2\"}";
 				//the parameter for xs, which format is : 		{
			    // 												'1': 'x0',
			    // 												'2': 'x1',
			    // 												},
 				var xsPara = '{';
 				for (var i = 0; i < performances.length - 1; i++) {
 					xsPara = xsPara + "\"" + datas[i] + "\":" + "\"" + xValue[i] + "\",";
 				}
 				xsPara = xsPara + "\"" + datas[performances.length - 1] + "\":" + "\"" + xValue[performances.length - 1] + "\"}";

				var ob = JSON.parse(xsPara);
				//console.log(ob); 

				//the parameter for columns, which format is : [
		        //      			['x0', performances[0].performance[0].date,performances[0].performance[1].date],
		        //     				['x1', performances[1].performance[0].date,],
				//       			['1', performances[0].performance[0].value,performances[0].performance[1].value],
				//        			['2', performances[1].performance[0].value,performances[1].performance[1].value]
				//      		]
				//when use, we should choose proper value for cpu, mem and disk
				
				var cpuColumn = [];
				for (var i = 0; i < performances.length; i++) {
					var item = [xValue[i]];
					if ('cpu' in performances[i]) {
						for (var j = 0; j < performances[i].cpu.length; j++) {
							var datevalue = performances[i].cpu[j].date;
							var year = datevalue.substring(0, 4);
    						var month = parseInt(datevalue.substring(4, 6)) - 1;							              
    						var day = datevalue.substring(6, 8);
    						var hour = datevalue.substring(8, 10);							    
    						var minute = datevalue.substring(10, 12);
    						var second = datevalue.substring(12, 14);

    						var d = new Date(year,month,day,hour,minute,second,0);

    						var dateEpoch = d.getTime();

							item.push(dateEpoch);
							//item.push(performances[i].cpu[j].date);
						}
						cpuColumn.push(item);
					}		
				}
				for (var i = 0; i < performances.length; i++) {
					var item = [datas[i]];
					if ('cpu' in performances[i]) {
						for (var j = 0; j < performances[i].cpu.length; j++) {
							item.push(performances[i].cpu[j].value);
						}
						cpuColumn.push(item);
					}
				}

				var memrColumn = [];
				for (var i = 0; i < performances.length; i++) {
					var item = [xValue[i]];
					if ('memr' in performances[i]) {
						for (var j = 0; j < performances[i].memr.length; j++) {
							var datevalue = performances[i].memr[j].date;
							var year = datevalue.substring(0, 4);
    						var month = parseInt(datevalue.substring(4, 6)) - 1;							              
    						var day = datevalue.substring(6, 8);
    						var hour = datevalue.substring(8, 10);							    
    						var minute = datevalue.substring(10, 12);
    						var second = datevalue.substring(12, 14);

    						var d = new Date(year,month,day,hour,minute,second,0);

    						var dateEpoch = d.getTime();

							item.push(dateEpoch);
						}
						memrColumn.push(item);	
					}
				}
				for (var i = 0; i < performances.length; i++) {
					var item = [datas[i]];
					if ('memr' in performances[i]) {
						for (var j = 0; j < performances[i].memr.length; j++) {
							item.push(performances[i].memr[j].value);
						}
						memrColumn.push(item);
					}
				}

				var memwColumn = [];
				for (var i = 0; i < performances.length; i++) {
					var item = [xValue[i]];
					if ('memw' in performances[i]) {
						for (var j = 0; j < performances[i].memw.length; j++) {
							var datevalue = performances[i].memw[j].date;
							var year = datevalue.substring(0, 4);
    						var month = parseInt(datevalue.substring(4, 6)) - 1;							              
    						var day = datevalue.substring(6, 8);
    						var hour = datevalue.substring(8, 10);							    
    						var minute = datevalue.substring(10, 12);
    						var second = datevalue.substring(12, 14);

    						var d = new Date(year,month,day,hour,minute,second,0);

    						var dateEpoch = d.getTime();

							item.push(dateEpoch);
						}
						memwColumn.push(item);
					}
				}
				for (var i = 0; i < performances.length; i++) {
					var item = [datas[i]];
					if ('memr' in performances[i]) {
						for (var j = 0; j < performances[i].memw.length; j++) {
							item.push(performances[i].memw[j].value);
						}
						memwColumn.push(item);
					}
				}

				var dioColumn = [];
				for (var i = 0; i < performances.length; i++) {
					var item = [xValue[i]];
					if ('dio' in performances[i]) {
						for (var j = 0; j < performances[i].dio.length; j++) {
							var datevalue = performances[i].dio[j].date;
							var year = datevalue.substring(0, 4);
    						var month = parseInt(datevalue.substring(4, 6)) - 1;							              
    						var day = datevalue.substring(6, 8);
    						var hour = datevalue.substring(8, 10);							    
    						var minute = datevalue.substring(10, 12);
    						var second = datevalue.substring(12, 14);

    						var d = new Date(year,month,day,hour,minute,second,0);

    						var dateEpoch = d.getTime();

							item.push(dateEpoch);
						}
						dioColumn.push(item);
					}
				}
				for (var i = 0; i < performances.length; i++) {
					var item = [datas[i]];
					if ('memr' in performances[i]) {
						for (var j = 0; j < performances[i].dio.length; j++) {
							item.push(performances[i].dio[j].value);
						}
						dioColumn.push(item);
					}
				}
				
	 			$('#myModal').on('shown.bs.modal', function(e) {
					
					cpuchart = c3.generate({
					    bindto: '#cpuChart',

					    data: { 
				          xs: ob,
					      columns : cpuColumn
					    },
					    axis: { 
                			x: { 
                        		type: 'timeseries', 
                        		tick: { 
                                	format: '%Y-%m-%d %H:%M:%S', 
                    		    }		 
                			}	 
        				}, 
					    zoom: {
					        enabled: true
					    },
					    subchart: {
					        show: true
					    },
					    legend: {
					        position: 'bottom'
					    }
					});
					
					memrchart = c3.generate({
					    bindto: '#MemoryReadChart',
					    
					    data: { 
				          xs: ob,
					      columns : memrColumn
					    },
					    axis: { 
                			x: { 
                        		type: 'timeseries', 
                        		tick: { 
                                	format: '%Y-%m-%d %H:%M:%S', 
                    		    }		 
                			}	 
        				}, 
					    zoom: {
					        enabled: true
					    },
					    subchart: {
					        show: true
					    },
					    legend: {
					        position: 'bottom'
					    }
					});

					memwchart = c3.generate({
					    bindto: '#MemoryWriteChart',
					    
					    data: { 
				          xs: ob,
					      columns : memwColumn
					    },
					    axis: { 
                			x: { 
                        		type: 'timeseries', 
                        		tick: { 
                                	format: '%Y-%m-%d %H:%M:%S', 
                    		    }		 
                			}	 
        				}, 
					    zoom: {
					        enabled: true
					    },
					    subchart: {
					        show: true
					    },
					    legend: {
					        position: 'bottom'
					    }
					});

					diochart = c3.generate({
					    bindto: '#DiskChart',
					    
					    data: { 
				          xs: ob,
					      columns : dioColumn
					    },
					    axis: { 
                			x: { 
                        		type: 'timeseries', 
                        		tick: { 
                                	format: '%Y-%m-%d %H:%M:%S', 
                    		    }		 
                			}	 
        				}, 
					    zoom: {
					        enabled: true
					    },
					    subchart: {
					        show: true
					    },
					    legend: {
					        position: 'bottom'
					    }
					});	  	
				});

				$("#alltabs").tabs({
	    			activate: function (event, ui) {
	        			
	    				var network = null;
	    				var size = 10;
	    				var smooth = {enabled: false};
	    				// for(var i = 0; i < performances.length; i++) {

	    				// }
	    				for(var i = 0; i < performances.length; i++) {
	    					
	    					if('net' in performances[i]) {
	    						var containerID = "Network-" + i +"Graph";
	    						var nnodes = new Array();
			    				var eedges = new Array();
			    				
			    				nnodes[0] = {id: performances[i].id, size: size, label: performances[i].id};
			    				for(var j = 0; j < performances[i].net.length; j++) {
			    					var tmp = {id: performances[i].net[j].node, size: size, label: performances[i].net[j].node};
			    					nnodes[j+1] = tmp;
			    				}
			    				for(var j = 0; j < performances[i].net.length; j++) {
			    					var tmp = {from: performances[i].id, to: performances[i].net[j].node, length: 100, title: performances[i].net[j].value, smooth: smooth};
			    					eedges[j] = tmp;
			    				}
			    			    console.log(nnodes);
			    				console.log(eedges);
							    // Instantiate our network object.
							    console.log(containerID);
							    var container = document.getElementById(containerID);
							    console.log(container);
							    var data = {
							        nodes: nnodes,
							        edges: eedges
							    };
							    var options = {
							        nodes: {
							        	shape: 'dot',
							        }
							    };
							    if(container != null) {
							    	network = new vis.Network(container, data, options);
							    }
	    					}
	    				}	  	
					}   //end of activate: function
				}); //end of $("#alltabs").tabs
			},  //end of success : function(resp)
    	});  //end of $.ajax
    });  //end of $('#display').click

    
    var oTable = $('#instanceStatus').DataTable({
        "processing": true,
        // "serverSide": true,
        "ajax": "status",

        columns: [
	        { data: 'instanceDNS' },
	        { data: 'status' },
	    ]
    });
    //enable click choose
    $('#instanceStatus tbody').on( 'click', 'tr', function () {
        $(this).toggleClass('selected');
    } );
    //refresh the table
    $('#refresh').click(function() {
    	oTable.ajax.reload();
    });
});