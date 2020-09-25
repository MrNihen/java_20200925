app.service("indexService",function($http){
    //1.查看用户
    this.findName=()=>{
        return $http.get("../findName.do");
    }
})