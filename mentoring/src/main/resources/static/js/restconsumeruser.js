var appConsumer= angular.module( 'appConsumer' ,[] );

appConsumer.config(function($httpProvider) {
    //Enable cross domain calls
    $httpProvider.defaults.useXDomain = true;
});

appConsumer.controller("busquedaController", function($scope, $http){
	
	$scope.mostrarBusqueda=false;
	$scope.areaseleccioanda = "sin";
	$scope.institucionseleccionada = "sin";
	$scope.horasmes=0.0;
	
	$scope.activarBusqueda = function(){
		if($scope.mostrarBusqueda) $scope.mostrarBusqueda=false
		else $scope.mostrarBusqueda=true;
	}
	
	$scope.buscar = function(){
		$scope.cargandoBusqueda = true;
		console.log("Consulta lanzada")
		$http.get("/user/busqueda/"+$scope.areaseleccioanda+"/"+$scope.institucionseleccionada+"/"+$scope.horasmes).then(
			function sucessCallback(response){
				console.log(response.data);
				$scope.mentores = response.data;
				//console.log(data);
				for (var i=0;i<response.data.length;i++){
					$scope.mentores[i].expandido = false;
					$scope.mentores[i].obtenido = false;
				}
				
				$scope.usuarios=response.data;
				$scope.cargandoBusqueda = false;
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
	}
	
	$scope.obtenerMentor = function(mentor){
		if(mentor.obtenido){
			mentor.expandido=true;
		}
		else{
			$scope.cargandoBusqueda = true;
			console.log("Consulta lanzada")
			$http.post("/user/obtenermentor", mentor.correo).then(
				function sucessCallback(response){
					console.log(response.data);
					mentor.expandido = true;
					mentor.obtenido = true;
					mentor.info = response.data;
				
					$scope.cargandoBusqueda = false;
				},
				function errorCallback(response){
					console.log("Fallo al acceder")
					console.log(response)
				}
			)
		}
	}
	
	$scope.plegarMentor = function(mentor){
			mentor.expandido=false;
	}
	
	
	
	
	
	const coleccion = new Map();
	
	$scope.getUsuarios = function(){
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/api/busqueda").then(
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
				coleccion.get(id).form = {};
				$scope.cargando = false;
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
	}
	
	
	$scope.addNotificacion = function (usuario){
		//console.log(usuario.form);
		//console.log(usuario);	
		var notenvio = {"usuario" : usuario.id, "descripcion" : usuario.form.descripcion}
		$http.post("/api/busqueda/notificaciones/add",notenvio).then(
			function sucessCallback(response){
				if(response.status == 200){
					console.log(response);
				}
				
			},
			function errorCallback(response){
				console.log("Fallo al eliminar");
				console.log(response);
			}
		)
	}
	
});

appConsumer.controller("notificacionController", function($scope, $http){
	
	$scope.mostrarsin = false;
	var lastload = new Date();
	
	var actualizar = function(){
		$http.get("/user/notificaciones/"+lastload).then(
			function sucessCallback(response){
				if(response.status == 200){
					console.log(response.data);
					lastload = Date.now();
					for (var i=0;i<response.data.length;i++){
						$scope.notificaciones.push(response.data[i]); 
					}
				}
				
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
	}
	
	$scope.iniciaNotificaciones = function(){
		console.log("Consulta lanzada")
		$http.get("/user/notificaciones").then(
			function sucessCallback(response){
				console.log(response.data);
				$scope.notificaciones=response.data;
				$scope.cargando = false;
				lastload = Date.now();
				console.log(lastload);
				if($scope.notificaciones.length == 0){
					$scope.mostrarsin = true;
				}
				$scope.id = setInterval(() => {
					console.log(lastload);
					actualizar();
				}, 10000);
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
		
	}
	//https://stackoverflow.com/questions/16150289/running-angularjs-initialization-code-when-view-is-loaded
	//Llamamos a la función nada más cargar
	$scope.iniciaNotificaciones();
	
	$scope.detenActualizacion = function(){
		if ($scope.id) {
    		clearInterval(this.id);
  		}
	}
	
	$scope.$on("$destroy", function(){
  		$scope.detenActualizacion();
	});
	
	//JSON.stringify({"id":id})
	
	$scope.borrarNotificacion = function (notificacion){

		$http.post("/user/notificaciones/delete",notificacion.id).then(
			function sucessCallback(response){
				if(response.status == 200){
					console.log(response.data);
					index = $scope.notificaciones.indexOf(notificacion);
					$scope.notificaciones.splice(index,1);
				}
				
			},
			function errorCallback(response){
				console.log("Fallo al eliminar");
				console.log(response);
			}
		)
	}
	
	
	
	
});