$(document).ready(function(){
	//the example table
	var table = $('#example').DataTable();
    $('#example tbody').on( 'click', 'tr', function () {
        $(this).toggleClass('selected');
    } );
 
 	$('#display').click( function () {
 		//performances is used to store the return value from the server
 		var performances = [];
 		//get the selected instance in the table and make a json which will be send to server
 		var selectInstance = [];
    	for (var i = 0; i < oTable.rows('.selected').data().length; i++) {
    		console.log(oTable.rows('.selected').data()[i].name);
    		selectInstance.push(oTable.rows('.selected').data()[i].name);
    	}  
    	// var selectInstanceJson1 = JSON.stringify(selectInstance);
    	// console.log(selectInstanceJson1);
    	var selectInstanceObject = new Object();
    	selectInstanceObject.selected = selectInstance;
    	//console.log(selectInstanceObject);
    	var selectInstanceJson = JSON.stringify(selectInstanceObject);
    	console.log(selectInstanceJson); 
    	$.ajax({
    		type: 'get',
    		url: 'performance',
    		dataType: 'JSON',
    		data: {
    			selected: selectInstanceJson
    		},
    		success : function(resp) {
	 			//now for debug, the json is 
	 			//[{"id":"1","performance":[{"date":"1","value":"1"},{"date":"2","value":"2"}]},{"id":"2","performance":[{"date":"1","value":"2"},{"date":"2","value":"1"}]}]
	 			performances = resp;
	 			//console.log(performances.length);
	 			
	 			$('#myModal').on('shown.bs.modal', function(e) {

	 				var datas = new Array();
					var xValue = new Array();
					
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
					var memColumn = [];
					for (var i = 0; i < performances.length; i++) {
						var item = [xValue[i]];
						for (var j = 0; j < performances[i].performance.length; j++) {
							item.push(performances[i].performance[j].date);
						}
						memColumn.push(item);
					}
					for (var i = 0; i < performances.length; i++) {
						var item = [datas[i]];
						for (var j = 0; j < performances[i].performance.length; j++) {
							item.push(performances[i].performance[j].value);
						}
						memColumn.push(item);
					}
					//console.log(memColumn);

					//show how one line chart works with json input
			        var cpuchart = c3.generate({
					    bindto: '#cpuChart',
					    data: {
					    	json: resp[1].performance,
						    keys:{
						    	x:'date',
						    	value: ['value'],
						    },
					    },
					    zoom: {
					        enabled: true
					    },
					    subchart: {
					        show: true
					    },
					    legend: {
					        position: 'right'
					    }
					});
					
					//show how multiple lines chart works with different x axis value
					var memchart = c3.generate({
					    bindto: '#MemoryChart',
					    
					    data: { 
				          xs: ob,
					      columns : memColumn
					    },
					    zoom: {
					        enabled: true
					    },
					    subchart: {
					        show: true
					    },
					    legend: {
					        position: 'right'
					    }
					});

					//show how the c3.generate() function get parameters from outside
					var target = "\"#DiskChart\"";
					var text = '{"bindto":'+target+',"data":{"columns":[["data1",30,200,100,400,150,250]]}}';
					var obj = JSON.parse(text);
					var diskchart = c3.generate(obj);
					// var diskchart = c3.generate({
					//     bindto: '#DiskChart',
					//     data: {
					//         xs: {
					//             'data[1]': 'x1',
					//             'data[2]': 'x2',
					//         },
					//         columns: [
					//             ['x1', 10, 30, 45, 50, 70, 100],
					//             ['x2', 30, 50, 75, 100, 120],
					//             ['data[1]', 30, 200, 100, 400, 150, 250],
					//             ['data[2]', 20, 180, 240, 100, 190]
					//         ]
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
				});
			},
    	});
    });
	
	//
    $('#button').click( function () {
    	var selectInstance = [];
    	for (var i = 0; i < oTable.rows('.selected').data().length; i++) {
    		console.log(oTable.rows('.selected').data()[i].name);
    		selectInstance.push(oTable.rows('.selected').data()[i].name);
    	}  
    	var selectInstanceObject = new Object();
    	selectInstanceObject.selected = selectInstance;
    	console.log(selectInstanceObject);
    	var selectInstanceJson = JSON.stringify(selectInstanceObject);
    	console.log(selectInstanceJson); 	
        //alert( oTable.rows('.selected').data().length +' row(s) selected' );
 //        $.get('search', 
	// 		function(resp) {
	// 			// $('#instanceStatus').dataTable({
	// 		 //        // "aoColumns": [
	// 		 //        //   { "bSortable": false },
	// 		 //        //   null, null, null, null
	// 		 //        // ]
	// 		 //         data: resp,
	// 		 //         columns: [
	// 			//         { data: 'name' },
	// 			//         { data: 'position' },
	// 			//         { data: 'salary' },
	// 			//         { data: 'office' }
	// 			//     ]
	// 		 //    });
	// 		// 		$("#td1").append(resp);
	// 	 });
     } );

    //show how tables work with json input, the json format should like:
    //{"data":[{"name":"Mengye Gong","position":"System Architect","salary":"$3,120","start_date":"2011/04/25","office":"Edinburgh"},
    //         {"name":"Wei Li","position":"Software Developer","salary":"$5,120","start_date":"2013/04/4","office":"Fremond"}]}
    var oTable = $('#instanceStatus').DataTable({
        "processing": true,
        // "serverSide": true,
        "ajax": "status",

         columns: [
	        { data: 'name' },
	        { data: 'position' },
	        { data: 'salary' },
	        { data: 'office' },
	        { data: 'start_date' },
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