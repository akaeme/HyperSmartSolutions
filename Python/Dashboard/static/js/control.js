var socket = io.connect('http://' + document.domain + ':' + location.port);

socket.emit('control', '',function (data) {
	process_data(data);
});
var modal = document.getElementById('epcs');

window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}

function process_data(data){
	products_tmp = data['products'];
    epcs_tmp = data['epc'];
    for(var key in products_tmp) {
        var value = products_tmp[key];
        add_row_products(key, value[0], value[2]);
        var epc = epcs_tmp[key];
    }
}

function add_row_products(gtin14, product_name, unit, price) {
    var table = document.getElementById("table_all");
    var row = table.insertRow(-1);
    var gtin14_cell = row.insertCell(0);
    var product_name_cell = row.insertCell(1);
    var unit_cell = row.insertCell(2);
    var price_cell = row.insertCell(3);
    gtin14_cell.innerHTML = gtin14;
    product_name_cell.innerHTML = product_name;
    unit_cell.innerHTML = unit;
    var buttonElement = document.createElement('input');
	buttonElement.type = "button";
	buttonElement.id = gtin14;
	buttonElement.className = "btn btn-default btn-xs";
	buttonElement.value = "View Epc";
	buttonElement.addEventListener('click', function(){
	    viewEpc(gtin14);
	});
    price_cell.appendChild(buttonElement);
}

function viewEpc(gtin14){
	var list = document.getElementById("epcs_list");
	var items = document.getElementById("epcs_list").getElementsByTagName("li").length;
	for (var i = 0; i < items; i++) {
		list.removeChild(document.getElementById(i));
	}
	socket.emit('retrieve_epcs', gtin14 ,function (data) {
		for (var i = 0; i < data.length; i++) {
			var li = document.createElement("li");
			console.log(data[i]);
			li.setAttribute("id", i);
			li.appendChild(document.createTextNode(data[i]));
			list.appendChild(li);
		}
	});
}