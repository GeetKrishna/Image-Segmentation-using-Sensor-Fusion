function showGraph(ja){

    var data=[];

    var keys = Object.keys(ja);
    for(var i=0;i<keys.length;i++){
        var key = keys[i];
         data.push({"name":key,"y":parseInt(ja[key])});
    }

     Highcharts.chart('container', {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie'
            },
            title: {
                text: 'Food Nutrition'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false
                    },
                    showInLegend: true
                }
            },
            series: [{
             name: 'Subjects',
                       colorByPoint: true,
                       data: data
            }]
        });
    }
