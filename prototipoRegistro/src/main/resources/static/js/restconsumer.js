//var aplicacionPrueba = angular.module( 'appPrueba' ,["ngResouce"] );
var aplicacionPrueba = angular.module( 'appPrueba' ,[] );

aplicacionPrueba.controller("consumerController", function($scope, $http){
	
	const coleccion = new Map();
	
	$scope.getUsuarios = function(){
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/api/Usuarios").then(
			function sucessCallback(response){
				//console.log(data);
				for (var i=0;i<response.data.length;i++){
					response.data[i].expandido = false;
					coleccion.set(response.data[i].id,response.data[i]);
				}
				console.log(response.data);
				$scope.usuarios=response.data;
				$scope.cargando = false;
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
	}
	
	$scope.getPerfil = function(id){
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/api/Usuarios/"+id).then(
			function sucessCallback(response){
				console.log(response.data);
				coleccion.get(id).expandido=true;
				coleccion.get(id).info=response.data;
				$scope.cargando = false;
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
	}
	
});
//$http.get("http://localhost:8080/api/busqueda").then(
/*aplicacionPrueba.controller("consumerController", function($scope, $resource){
	var Consumidor = $resource("/api/busqueda");
	var Consumidor2 = $resource("/api/Usuarios");
	
	
	
	$scope.getUsuarios = function(){
		$scope.cargando = true;
		console.log("Cargando");
		var users = Consumidor.query();
		users.$promise.then(function (){
			$scope.usuarios = users;
			$scope.cargando = false;
		});
	}
	$scope.getUsuariosCompleto = function(){
		$scope.cargando = true;
		console.log("Cargando");
		var users = Consumidor2.query();
		//$scope.usuariosCompleto = users;
		users.$promise.then(function (){
			console.log(users);
			$scope.cargando = false;
		});
	}
	
});*/