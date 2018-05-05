function showGraph(ja){

    var data=[];
    var userNutritionList=[];

    var keys = Object.keys(ja);
    var val;
    for(var i=0;i<keys.length;i++){
        var key = keys[i];
        userNutritionList.push(ja[key]);
         data.push({name:key,data:ja[key]});
    }

var answer_counts= [
            {name: 'Ideal Nutrition', data : idealNutritionList},
            {name: 'User\'s Nutrition', data: userNutritionList}];

var myChart = Highcharts.chart('container', {
        chart: {
        renderTo: 'container',

            type: 'column'
        }, title: {
                          text: 'Food Nutrition Diary'
                      },

        xAxis: {
            categories: keys
        },

        yAxis: {

                            min: 0,
                            title: {
                                text: 'Nutrition Content for the day'
                            }

        },
        series: answer_counts
    });
}



