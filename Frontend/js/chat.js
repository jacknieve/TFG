var appChat= angular.module( 'appChat' ,[] );

appChat.controller("chatController", function($scope){
	
	$scope.kk = function(kk){
		$scope.mostrar = kk;
	}
	
	
	});