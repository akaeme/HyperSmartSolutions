var socket = io.connect('http://' + document.domain + ':' + location.port);

socket.on('request', function(data) {
    process_row(data);
});

var g1, g2;
var max_capacity = 200;
var units = 0;
var labels = [];
var series = [];
let currentdate = getStringLabel();
labels.push(currentdate);
series.push(units);

var chart = new Chartist.Line('.ct-chart', {
  labels: labels,
  series: [series]
}, {
  fullWidth: true,
  chartPadding: {
    right: 60
  },
  axisY: {
    onlyInteger: true,
    offset: 20
  }
});

window.onload = function(){
    socket.emit('load', '',function (data) {
        console.log(data);
        units = data['unit'];
        g1 = new JustGage({
          id: "g1",
          value: units,
          min: 0,
          max: 500,
          levelColors: [
              "#DBCFB0",
              "#BFC8AD",
              "#90B494",
              "#718F94",
              "#545775"
            ],
          gaugeWidthScale: 0.3
        });
        g2 = new JustGage({
          id: "g2",
          value: 100,
          min: 0,
          max: 100,
          label: "Percent",
          levelColors: [
              "#EF7A7A",
              "#F7D274",
              "#9FF2A1",
              "#99C7FC",
              "#F2B2F4"
          ],
          gaugeWidthScale: 0.3
        });
        products_tmp = data['products'];
        epcs_tmp = data['epc'];
        for(var key in products_tmp) {
            var value = products_tmp[key];
            add_row_products(key, value[0], value[2], value[1]);
            var epc = epcs_tmp[key];
            add_row_epc(epc, key);
        }
    });
    
};

function addZero(i) {
    if (i < 10) {
        i = "0" + i;
    }
    return i;
}

function getStringLabel() {
    let d = new Date();
    let h = addZero(d.getHours());
    let m = addZero(d.getMinutes());
    let s = addZero(d.getSeconds());
    return  h + ":" + m + ":" + s;
}

function process_row(request) {
    gtin14 = request['gtin14'];
    product_name = request['name'];
    unit = request['unit'];
    price = request['price'];
    epcs = request['epc'];
    add_row_products(gtin14, product_name, unit, price);
        
    // add only the last one
    add_row_epc(epcs[epcs.length - 1], gtin14);
   
    units += unit;
    g1.refresh(units);
    var percent = (max_capacity - units)/max_capacity*100;
    g2.refresh(percent);
    let currentdate = getStringLabel();
    labels.push(currentdate);
    series.push(units);
    chart.update({
      labels: labels,
      series: [series]
    });
}

function add_row_products(gtin14, product_name, unit, price) {
    var table = document.getElementById("table_products");
    var count = document.getElementById("table_products").getElementsByTagName("tr").length;
    if (count == 5){
        for (var i = 0; i < 5; i++) {
            table.deleteRow(0);
        }
    }
    var row = table.insertRow(-1);
    var gtin14_cell = row.insertCell(0);
    var product_name_cell = row.insertCell(1);
    var unit_cell = row.insertCell(2);
    var price_cell = row.insertCell(3);
    gtin14_cell.innerHTML = gtin14;
    product_name_cell.innerHTML = product_name;
    unit_cell.innerHTML = unit;
    price_cell.innerHTML = price;
}

function add_row_epc(epc, gtin14) {
    var table = document.getElementById("table_epc");
    var count = document.getElementById("table_epc").getElementsByTagName("tr").length;
    if (count == 5){
        for (var i = 0; i < 5; i++) {
            table.deleteRow(0);
        }
    }
    var row = table.insertRow(-1);
    var epc_cell = row.insertCell(0);
    var gtin14_cell = row.insertCell(1);
    epc_cell.innerHTML = epc;
    gtin14_cell.innerHTML = gtin14;
}