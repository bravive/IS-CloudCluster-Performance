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
	})

 	$('#display').click( function () {
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
	 			//console.log(performances);
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
		        //     				['x1', performances[1].performance[0].date,3],
				//       			['1', performances[0].performance[0].value,performances[0].performance[1].value],
				//        			['2', performances[1].performance[0].value,performances[1].performance[1].value]
				//      		]
				//when use, we should choose proper value for cpu, mem and disk
				
				var cpuColumn = [];
				for (var i = 0; i < performances.length; i++) {
					var item = [xValue[i]];
					if ('cpu' in performances[i]) {
						for (var j = 0; j < performances[i].cpu.length; j++) {
							item.push(performances[i].cpu[j].date);
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
							item.push(performances[i].memr[j].date);
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
							item.push(performances[i].memw[j].date);
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
							item.push(performances[i].dio[j].date);
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
	 				//console.log("modal on");
	 				
					// var memColumn = [];
					// for (var i = 0; i < performances.length; i++) {
					// 	var item = [xValue[i]];
					// 	for (var j = 0; j < performances[i].performance.length; j++) {
					// 		item.push(performances[i].performance[j].date);
					// 	}
					// 	memColumn.push(item);
					// }
					// for (var i = 0; i < performances.length; i++) {
					// 	var item = [datas[i]];
					// 	for (var j = 0; j < performances[i].performance.length; j++) {
					// 		item.push(performances[i].performance[j].value);
					// 	}
					// 	memColumn.push(item);
					// }
					

					//show how one line chart works with json input
			  //       var cpuchart = c3.generate({
					//     bindto: '#cpuChart',
					//     data: {
					//     	json: resp[0].cpu,
					// 	    keys:{
					// 	    	x:'date',
					// 	    	value: ['value'],
					// 	    },
					//     },
					//     zoom: {
					//         enabled: true
					//     },
					//     subchart: {
					//         show: true
					//     },
					//     legend: {
					//         position: 'right'
					//     }
					// });
					
					cpuchart = c3.generate({
					    bindto: '#cpuChart',

					    data: { 
				          xs: ob,
					      columns : cpuColumn
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
					
					//show how multiple lines chart works with different x axis value
					memrchart = c3.generate({
					    bindto: '#MemoryReadChart',
					    
					    data: { 
				          xs: ob,
					      columns : memrColumn
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

					//show how the c3.generate() function get parameters from outside
					// var target = "\"#DiskChart\"";
					// var text = '{"bindto":'+target+',"data":{"columns":[["data1",30,200,100,400,150,250]]}}';
					// var obj = JSON.parse(text);
					// var diskchart = c3.generate(obj);
				});
			},
    	});
    });

    
    var oTable = $('#instanceStatus').DataTable({
        "processing": true,
        // "serverSide": true,
        "ajax": "status",

        columns: [
	        { data: 'instanceDNS' },
	        { data: 'status' },
	    ]
	//show how tables work with json input, the json format should like:
    //{"data":[{"name":"Mengye Gong","position":"System Architect","salary":"$3,120","start_date":"2011/04/25","office":"Edinburgh"},
    //         {"name":"Wei Li","position":"Software Developer","salary":"$5,120","start_date":"2013/04/4","office":"Fremond"}]}
     //     columns: [
	    //     { data: 'name' },
	    //     { data: 'position' },
	    //     { data: 'salary' },
	    //     { data: 'office' },
	    //     { data: 'start_date' },
	    // ]

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