app.controller("indexController",function($scope,indexService){
    //1.查看用户名
    $scope.findName=()=>{
        indexService.findName().success(response=>{
            $scope.name = response.name;
        })
    }

})
