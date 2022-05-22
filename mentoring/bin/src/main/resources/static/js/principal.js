var appRegister= angular.module( 'appPrincipal' ,[] );

appRegister.controller("principalController", function($scope){
	
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	const mentorParam = urlParams.get('m');
	if(mentorParam == 1) $scope.mentor=true;
	else $scope.mentor=false;
	
	});