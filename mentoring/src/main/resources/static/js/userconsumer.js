var appConsumer= angular.module( 'appConsumer' ,[] );

appConsumer.config(function($httpProvider) {
    //Enable cross domain calls
    $httpProvider.defaults.useXDomain = true;
});

appConsumer.controller("userController", function($scope, $http){
	
	//Mapa para acceder directamente a las areas de usuario
	const areasUsuario = new Map();
	//Mapa para ver si un area es nueva al borrar, es decir, si aun no se ha guardado en el backend
	const areasNuevas = new Map();
	var copiaDatos;
	$scope.confirmarBorrar=false;
	
	$scope.getInfo = function(){
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/user/info").then(
			function sucessCallback(response){
				if(response.status == 200){
					//console.log(response);
					console.log(response.data);
					$scope.usuario=response.data;
					copiaDatos = Object.assign({}, response.data);
					$scope.mydate = new Date(response.data.fnacimiento); 
					//Aqui pasamos las areas a un mapa, para acceder directamente al añadir o borrar
					if(response.data.areas.length>0){
						for (var i=0;i<response.data.areas.length;i++){
							areasUsuario.set(response.data.areas[i].area,response.data.areas[i]);
						}
					}
					else{
						$scope.sinareas=true;
					}
					$scope.areaseleccioanda = "--Escoge una--";
				}
				$scope.cargando = false;				
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
				console.log(response)
			}
		)
	}
	
	$scope.getInfo();
	
	$scope.setInfo = function(usuario){
		console.log($scope)
		if($scope.form.$valid){
		$scope.cargando = true;
		console.log("Consulta lanzada")
		usuario.fnacimiento = $scope.mydate;
		$http.post("/user/setinfo", usuario).then(
			function sucessCallback(response){
				if(response.status == 200){
					//console.log(response);
					console.log(response);
					areasNuevas.clear(); //Borramos las areas para no intentar solo eliminar del frontend un area
					alert("La operacion ha sido un exito");
				}
				else{
					$scope.sinareas=true;
				}
				$scope.cargando = false;				
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
				console.log(response)
			}
		)
		}
		else{
			alert("Por favor, introduzca valores válidos en los campos");
		}
	}
	
	$scope.deshacer = function (){
		console.log(copiaDatos);
		console.log($scope.usuario);
		$scope.usuario = Object.assign({}, copiaDatos);
	}
	
	//Cambiarlo a add area
	$scope.addArea = function (areaSelecionada){
		$scope.sinareas=false;
		if(areaSelecionada !== "--Escoge una--"){
			if(areasUsuario.has(areaSelecionada)){
				alert("Ya tienes esa area");
			}
			else{
				$scope.usuario.areas.push({area : areaSelecionada});
				areasUsuario.set(areaSelecionada,$scope.usuario.areas[$scope.usuario.areas.length - 1]);
				//Esto lo usamos para no tener que llamar a borrar cuando aun no se ha guardado un area
				areasNuevas.set(areaSelecionada,areaSelecionada);
			}
		}
	}
	
	
	$scope.borrarArea = function (area){
		if(areasNuevas.has(area.area)){
			index = $scope.usuario.areas.indexOf(area.area);
			$scope.usuario.areas.splice(index,1);
			areasUsuario.delete(area.area);
			areasNuevas.delete(area.area);
			console.log("Borrado solo en el frontend");
			if($scope.usuario.areas.length == 0){
				$scope.sinareas=true;
			}
		}
		else{
		$http.post("/user/areas/delete",area).then(
			function sucessCallback(response){
				if(response.status == 200){
					console.log(response.data);
					index = $scope.usuario.areas.indexOf(area.area);
					$scope.usuario.areas.splice(index,1);
					areasUsuario.delete(area.area);
					if($scope.usuario.areas.length == 0){
						$scope.sinareas=true;
					}
				}
				
			},
			function errorCallback(response){
				console.log("Fallo al eliminar");
				console.log(response);
			}
		)
		}
	}
	
});
