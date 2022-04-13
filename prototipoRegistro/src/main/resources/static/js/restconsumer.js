//var aplicacionPrueba = angular.module( 'appPrueba' ,["ngResouce"] );
var aplicacionPrueba = angular.module( 'appPrueba' ,[] );

aplicacionPrueba.config(function($httpProvider) {
    //Enable cross domain calls
    $httpProvider.defaults.useXDomain = true;
});

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
				coleccion.get(id).form = {};
				$scope.cargando = false;
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
	}
	
	$scope.addUser = function(){
		console.log("kk");
		var newuser= {id : 40, username : "prueba", edad : 50, area : "Kakota"};
		//coleccion.set(40,newuser);
		$scope.usuarios.push(newuser); 
		var newuser= {id : 41, username : "prueba", edad : 50, area : "Kakota"};
		//coleccion.set(40,newuser);
		$scope.usuarios.push(newuser); 
		var newuser= {id : 42, username : "prueba", edad : 50, area : "Kakota"};
		//coleccion.set(40,newuser);
		$scope.usuarios.push(newuser); 
		console.log($scope.usuarios);
	}
	
	//https://love2dev.com/blog/javascript-remove-from-array/
	$scope.removeUser = function(){
		console.log("kakota");
		var id = [40,41,42];
		$scope.usuarios = $scope.usuarios.filter(function(elemento){
			//return elemento.id != id;
			//Comprobamos que sus índices no existan en la lista para borrar
			return id.indexOf(elemento.id) == -1;
		})
		console.log($scope.usuarios);
	}
	
	$scope.addNotificacion = function (usuario){
		//console.log(usuario.form);
		//console.log(usuario);	
		var notenvio = {"usuario" : usuario.id, "descripcion" : usuario.form.descripcion}
		$http.post("/api/Usuarios/notificaciones/add",notenvio).then(
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

aplicacionPrueba.controller("notificacionController", function($scope, $http){
	
	$scope.mostrarnot = false;
	var lastload = new Date();
	
	var actualizar = function(){
		$http.get("/api/Usuarios/notificaciones/"+lastload).then(
			function sucessCallback(response){
				if(response.status == 200){
					console.log(response.data);
					lastload = Date.now();
					for (var i=0;i<response.data.length;i++){
						//Esto ver que compensa más, si hacerlo aqui o hacerlo en el backend
						//En el caso del backend, habria que devolver una clase con 2 listas
						var todelete = [];
						if(response.data[i].fechaeliminacion != null){
							todelete.push(response.data[i].id);
						}
						else{
							$scope.notificaciones.push(response.data[i]); 
						}
						if(todelete.length > 0){
							$scope.notificaciones = $scope.notificaciones.filter(function(elemento){
								return todelete.indexOf(elemento.id) == -1;
							});
						}
						
					}
				}
				
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
				$scope.detenActualizacion();
			}
		)
	}
	
	$scope.iniciaNotificaciones = function(){
		console.log("Consulta lanzada")
		$http.get("/api/Usuarios/notificaciones").then(
			function sucessCallback(response){
				console.log(response.data);
				$scope.notificaciones=response.data;
				$scope.cargando = false;
				lastload = Date.now();
				console.log(lastload);
				$scope.mostrarnot = true;
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
			}
		)
		this.id = setInterval(() => {
			console.log(lastload);
			actualizar();
		}, 10000);
	}
	//https://stackoverflow.com/questions/16150289/running-angularjs-initialization-code-when-view-is-loaded
	//Llamamos a la función nada más cargar
	$scope.iniciaNotificaciones();
	
	$scope.detenActualizacion = function(){
		if (this.id) {
    		clearInterval(this.id);
  		}
	}
	
	$scope.$on("$destroy", function(){
  		$scope.detenActualizacion();
	});
	
	//JSON.stringify({"id":id})
	
	$scope.borrarNotificacion = function (id){

		$http.post("/api/Usuarios/notificaciones/borrar",id).then(
			function sucessCallback(response){
				if(response.status == 200){
					console.log(response.data);
					actualizar();
				}
				
			},
			function errorCallback(response){
				console.log("Fallo al eliminar");
				console.log(response);
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