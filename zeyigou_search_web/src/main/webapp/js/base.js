var app=angular.module('zeyigou',[]);
//定义过滤器
app.filter("trustHtml",['$sce',function($sce){
    return function(data){
        return $sce.trustAsHtml(data);
    }
}])
