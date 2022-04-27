var appConsumer = angular.module('appConsumer', []);

appConsumer.config(function($httpProvider) {
	//Enable cross domain calls
	$httpProvider.defaults.useXDomain = true;
});

appConsumer.controller("busquedaController", function($scope, $http) {

	$scope.mostrarBusqueda = false;
	$scope.sinresultados = false;
	$scope.areaseleccioanda = "sin";
	$scope.institucionseleccionada = "sin";
	$scope.horasmes = 0.0;

	$scope.activarBusqueda = function() {
		if ($scope.mostrarBusqueda) $scope.mostrarBusqueda = false
		else $scope.mostrarBusqueda = true;
	}

	$scope.buscar = function() {
		$scope.cargandoBusqueda = true;
		console.log("Consulta lanzada")
		if ($scope.horasmes == null) $scope.horasmes = 0.0;
		$http.get("/mentorizado/busqueda/" + $scope.areaseleccioanda + "/" + $scope.institucionseleccionada + "/" + $scope.horasmes).then(
			function sucessCallback(response) {
				//Si la peticion tiene los Path variables mal, o no es correcto, suelta un 400, y si el ultimo es vacio, suelta un 404
				console.log(response.data);
				$scope.sinresultados = false;
				$scope.mentores = response.data;
				if (response.data.length > 0) {
					//console.log(data);
					for (var i = 0; i < response.data.length; i++) {
						$scope.mentores[i].expandido = false;
						$scope.mentores[i].obtenido = false;
					}

				} else {
					$scope.sinresultados = true;
				}

				$scope.cargandoBusqueda = false;
			},
			function errorCallback(response) {
				console.log(response)
				console.log("Fallo al acceder")
				$scope.cargandoBusqueda = false;
			}
		)
	}

	/*$scope.buscardos = function(){
		$scope.cargandoBusqueda = true;
		console.log("Consulta lanzada")
		$http.get("/mentorizado/busquedados/"+$scope.areaseleccioanda+"/"+$scope.institucionseleccionada+"/"+$scope.horasmes).then(
			function sucessCallback(response){
				console.log(response.data);
				$scope.mentoresdos = response.data;
				//console.log(data);
				for (var i=0;i<response.data.length;i++){
					$scope.mentoresdos[i].expandido = false;
					$scope.mentoresdos[i].obtenido = false;
				}
				
				$scope.cargandoBusqueda = false;
			},
			function errorCallback(response){
				console.log("Fallo al acceder")
				console.log(response)
			}
		)
	}*/

	$scope.obtenerMentor = function(mentor) {
		if (mentor.obtenido) {
			mentor.expandido = true;
		}
		else {
			$scope.cargandoBusqueda = true;
			console.log("Consulta lanzada")
			$http.post("/mentorizado/obtenermentor", mentor.correo).then(
				function sucessCallback(response) {
					console.log(response.data);
					mentor.expandido = true;
					mentor.obtenido = true;
					mentor.info = response.data;
					mentor.info.solicitud = false;
					mentor.info.motivo = "";

					$scope.cargandoBusqueda = false;
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
				}
			)
		}
	}

	$scope.plegarMentor = function(mentor) {
		mentor.expandido = false;
	}

	$scope.mostrarOcultarSolicitud = function(mentor) {
		if (mentor.info.solicitud) mentor.info.solicitud = false
		else mentor.info.solicitud = true;
	}


	$scope.enviarSolicitud = function(mentor, motivo) {
		console.log("Consulta lanzada")
		$http.post("/mentorizado/enviarsolicitud", { mentor: mentor, motivo: motivo }).then(
			function sucessCallback(response) {
				console.log(response.data);
				if (response.status == 200) {
					alert("Peticion enviada con exito");
				}
				/*mentor.expandido = true;
				mentor.obtenido = true;
				mentor.info = response.data;*/

			},
			function errorCallback(response) {
				if (response.status == 409) {
					alert("Ya has establecido una relacion de mentorizacion con este mentor");
				}
				console.log("Fallo al acceder")
				console.log(response)

			}
		)
	}

});

appConsumer.controller("peticionController", function($scope, $http) {

	$scope.mostrarPeticiones = false;
	$scope.sinresultados = false;
	$scope.cargandoBusqueda = false;
	var yaObtenidas = false;

	$scope.activarPeticiones = function() {
		if ($scope.mostrarPeticiones) {
			$scope.mostrarPeticiones = false
			yaObtenidas = true;
			$scope.detenActualizacion();
		}
		else {
			$scope.mostrarPeticiones = true;
			if (!yaObtenidas) {
				$scope.cargandoBusqueda = true;
				console.log("Consulta lanzada")
				$http.get("/mentor/peticiones/").then(
					function sucessCallback(response) {
						console.log(response.data);
						$scope.peticiones = response.data;
						if (response.data.length > 0) {
							for (var i = 0; i < response.data.length; i++) {
								$scope.peticiones[i].expandido = false;
								$scope.peticiones[i].obtenido = false;
								$scope.peticiones[i].enAccion = false;
							}

						} else {
							$scope.peticiones = [];
							$scope.sinresultados = true;
						}
						$scope.id = setInterval(() => {
							actualizar();
						}, 60000);
						$scope.cargandoBusqueda = false;
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						$scope.cargandoBusqueda = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)
			}
			else{
				actualizar();
			}
		}
	}

	var actualizar = function() {
		$http.get("/mentor/peticiones/actualizar").then(
			function sucessCallback(response) {
				if (response.status == 200) {
					console.log(response.data);
					$scope.sinresultados = false;
					for (var i = 0; i < response.data.length; i++) {
						$scope.peticiones.push(response.data[i]);
					}
					if($scope.peticiones.length == 0){
						$scope.sinresultados = true;
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				$scope.detenActualizacion;
			}
		)
	}

	$scope.detenActualizacion = function() {
		if ($scope.id) {
			clearInterval(this.id);
		}
	}

	$scope.$on("$destroy", function() {
		$scope.detenActualizacion();
	});



	$scope.obtenerPerfilPeticion = function(peticion) {
		if (peticion.obtenido) {
			peticion.expandido = true;
		}
		else {
			console.log("Consulta lanzada")
			$http.post("/mentor/peticiones/perfil", peticion.mentorizado).then(
				function sucessCallback(response) {
					console.log(response.data);
					peticion.expandido = true;
					peticion.obtenido = true;
					peticion.info = response.data;
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
				}
			)
		}
	}
	
	$scope.aceptarPeticion = function(peticion) {
		peticion.enAccion = true;
			console.log("Consulta lanzada")
			$http.post("/mentor/peticiones/aceptar", peticion.mentorizado).then(
				function sucessCallback(response) {
					console.log(response);
					if (response.status == 200) {
						index = $scope.peticiones.indexOf(peticion);
						$scope.peticiones.splice(index, 1);
						if($scope.peticiones.length == 0){
							$scope.sinresultados = true;
						}
					}
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					peticion.enAccion = false;
				}
			)
	}
	
	$scope.rechazarPeticion = function(peticion) {
		peticion.enAccion = true;
			console.log("Consulta lanzada")
			$http.post("/mentor/peticiones/rechazar", peticion.mentorizado).then(
				function sucessCallback(response) {
					console.log(response);
					if (response.status == 200) {
						index = $scope.peticiones.indexOf(peticion);
						$scope.peticiones.splice(index, 1);
						if($scope.peticiones.length == 0){
							$scope.sinresultados = true;
						}
					}
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					peticion.enAccion = false;
				}
			)
	}



	$scope.plegarPeticion = function(peticion) {
		peticion.expandido = false;
	}




});

appConsumer.controller("notificacionController", function($scope, $http) {

	$scope.mostrarsin = false;

	var actualizar = function() {
		$http.get("/user/notificaciones/nuevas").then(
			function sucessCallback(response) {
				if (response.status == 200) {
					$scope.mostrarsin = false;
					console.log(response.data);
					for (var i = 0; i < response.data.length; i++) {
						$scope.notificaciones.push(response.data[i]);
					}
					if($scope.notificaciones.length == 0){
						$scope.mostrarsin = true;
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				$scope.detenActualizacion();
			}
		)
	}

	$scope.iniciaNotificaciones = function() {
		console.log("Consulta lanzada")
		$http.get("/user/notificaciones").then(
			function sucessCallback(response) {
				console.log(response.data);
				$scope.notificaciones = response.data;
				$scope.cargando = false;
				if ($scope.notificaciones.length == 0) {
					$scope.notificaciones = [];
					$scope.mostrarsin = true;
				}
				$scope.id = setInterval(() => {
					actualizar();
				}, 10000);
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
			}
		)

	}
	//https://stackoverflow.com/questions/16150289/running-angularjs-initialization-code-when-view-is-loaded
	//Llamamos a la función nada más cargar
	$scope.iniciaNotificaciones();

	$scope.detenActualizacion = function() {
		if ($scope.id) {
			clearInterval(this.id);
		}
	}

	$scope.$on("$destroy", function() {
		$scope.detenActualizacion();
	});

	//JSON.stringify({"id":id})

	$scope.borrarNotificacion = function(notificacion) {

		$http.post("/user/notificaciones/delete", notificacion.id).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					console.log(response.data);
					index = $scope.notificaciones.indexOf(notificacion);
					$scope.notificaciones.splice(index, 1);
					if($scope.notificaciones.length == 0){
						$scope.mostrarsin = true;
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al eliminar");
				console.log(response);
			}
		)
	}




});


appConsumer.controller("mentorMentorizacionController", function($scope, $http) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargandoBusqueda = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.enAccion = false;

	$scope.activarMentorizaciones = function() {
		if ($scope.mostrarMentorizaciones) {
			$scope.mostrarMentorizaciones = false
			yaObtenidas = true;
			$scope.detenActualizacion();
		}
		else {
			$scope.enAccion = true;
			$scope.mostrarMentorizaciones = true;
			if (!yaObtenidas) {
				$scope.cargandoBusqueda = true;
				console.log("Consulta lanzada")
				$http.get("/mentor/mentorizaciones/").then(
					function sucessCallback(response) {
						lastload = Date.now();
						if (response.status == 200) {
							console.log(response.data);
							$scope.mentorizaciones = response.data;
							console.log(typeof($scope.mentorizaciones[0].fase));
							for (var i = 0; i < response.data.length; i++) {
								$scope.mentorizaciones[i].expandido = false;
								$scope.mentorizaciones[i].enAccion = false;
								$scope.mentorizaciones[i].aceptarcerrar = false;
							}
						} else if (response.status == 204){
							$scope.sinresultados = true;
							$scope.mentorizaciones = [];
						}
						$scope.id = setInterval(() => {
							actualizar();
						}, 60000);
						$scope.cargandoBusqueda = false;
						$scope.enAccion = false;
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						$scope.cargandoBusqueda = false;
						$scope.enAccion = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)
			}
			else{
				actualizar();
				$scope.enAccion = false;
			}
		}
	}

	var actualizar = function() {
		$http.get("/mentor/mentorizaciones/actualizar/"+lastload).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					lastload = Date.now();
					$scope.sinresultados = false;
					console.log(response.data);
					for (var i = 0; i < response.data.length; i++) {
						var todelete = [];
						if(response.data[i].uperfil == null){
							todelete.push(response.data[i].correo);
						}
						else{
							$scope.mentorizaciones.push(response.data[i]); 
						}
						if(todelete.length > 0){
							$scope.mentorizaciones = $scope.mentorizaciones.filter(function(elemento){
								return todelete.indexOf(elemento.correo) == -1;
							});
							
						}
						if($scope.mentorizaciones.length == 0){
							$scope.sinresultados = true;
						}
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				$scope.detenActualizacion();
			}
		)
	}

	$scope.detenActualizacion = function() {
		if ($scope.id) {
			clearInterval(this.id);
		}
	}

	$scope.$on("$destroy", function() {
		$scope.detenActualizacion();
	});



	$scope.plegarMentorizacion = function(mentorizacion) {
		if(mentorizacion.expandido) mentorizacion.expandido = false;
		else mentorizacion.expandido = true;
		
	}
	
	
	$scope.confirmarCerrar = function(mentorizacion) {
		if(mentorizacion.aceptarcerrar) mentorizacion.aceptarcerrar = false;
		else mentorizacion.aceptarcerrar = true;
		
	}


	$scope.cerrarMentorizacion = function(mentorizacion) {
				mentorizacion.enAccion = true;
				console.log("Consulta lanzada")
				$http.post("/mentor/mentorizaciones/cerrar", mentorizacion.correo).then(
					function sucessCallback(response) {
						console.log(response.data);
						index = $scope.mentorizaciones.indexOf(mentorizacion);
						$scope.mentorizaciones.splice(index, 1);
						if($scope.mentorizaciones.length == 0){
							$scope.sinresultados = true;
						}
						alert("La mentorizacion se ha cerrado con exito");
						
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						mentorizacion.enAccion = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)

	}


	$scope.aceptarCambioFase = function(mentorizacion) {
				mentorizacion.enAccion = true;
				console.log("Consulta lanzada")
				$http.post("/mentor/mentorizaciones/cambiarfase", {correo : mentorizacion.correo, fase : mentorizacion.fase}).then(
					function sucessCallback(response) {
						console.log(response.data);
						alert("La fase se ha cambiado con exito");
						
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						mentorizacion.enAccion = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)

	}


});

appConsumer.controller("mentorizadoMentorizacionController", function($scope, $http) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargandoBusqueda = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.enAccion = false;

	$scope.activarMentorizaciones = function() {
		
		if ($scope.mostrarMentorizaciones) {
			$scope.mostrarMentorizaciones = false
			yaObtenidas = true;
			$scope.detenActualizacion();
		}
		else {
			$scope.enAccion = true;
			$scope.mostrarMentorizaciones = true;
			if (!yaObtenidas) {
				$scope.cargandoBusqueda = true;
				console.log("Consulta lanzada")
				$http.get("/mentorizado/mentorizaciones/").then(
					function sucessCallback(response) {
						lastload = Date.now();
						if (response.status == 200) {
							console.log(response.data);
							$scope.mentorizaciones = response.data;
							for (var i = 0; i < response.data.length; i++) {
								$scope.mentorizaciones[i].expandido = false;
								$scope.mentorizaciones[i].enAccion = false;
								$scope.mentorizaciones[i].cerrar = false;
								$scope.mentorizaciones[i].comentario = "";
								$scope.mentorizaciones[i].puntuacion = null;
								$scope.mentorizaciones[i].aceptarcerrar = false;
							}
						} else if(response.status == 204){
							$scope.sinresultados = true;
							$scope.mentorizaciones = [];
						}
						$scope.id = setInterval(() => {
							actualizar();
						}, 60000);
						$scope.cargandoBusqueda = false;
						$scope.enAccion = false;
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						$scope.cargandoBusqueda = false;
						$scope.enAccion = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)
			}
			else{
				actualizar();
				$scope.enAccion = false;
			}
		}
	}

	var actualizar = function() {
		$http.get("/mentorizado/mentorizaciones/actualizar/"+lastload).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					lastload = Date.now();
					$scope.sinresultados = false;
					console.log(response.data);
					for (var i = 0; i < response.data.length; i++) {
						var todelete = [];
						if(response.data[i].uperfil == null){
							todelete.push(response.data[i].correo);
						}
						else{
							$scope.mentorizaciones.push(response.data[i]); 
						}
						if(todelete.length > 0){
							$scope.mentorizaciones = $scope.mentorizaciones.filter(function(elemento){
								return todelete.indexOf(elemento.correo) == -1;
							});
							
						}
						if($scope.mentorizaciones.length == 0){
							$scope.sinresultados = true;
						}
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				$scope.detenActualizacion();
			}
		)
	}

	$scope.detenActualizacion = function() {
		if ($scope.id) {
			clearInterval(this.id);
		}
	}

	$scope.$on("$destroy", function() {
		$scope.detenActualizacion();
	});



	$scope.plegarMentorizacion = function(mentorizacion) {
		if(mentorizacion.expandido) mentorizacion.expandido = false;
		else mentorizacion.expandido = true;
		
	}
	
	$scope.abrirPuntuar = function(mentorizacion) {
		if(mentorizacion.cerrar) {
			mentorizacion.cerrar = false;
			mentorizacion.aceptarcerrar = false;
		}
		else mentorizacion.cerrar = true;
		
	}
	
	$scope.confirmarCerrar = function(mentorizacion) {
			mentorizacion.aceptarcerrar = true;
		
	}


	$scope.cerrarMentorizacion = function(mentorizacion, form) {
		
		if(form.$valid){
				mentorizacion.enAccion = true;
				if(mentorizacion.puntuacion == null) mentorizacion.puntuacion=-1;
				console.log("Consulta lanzada")
				$http.post("/mentorizado/mentorizaciones/cerrar", {mentor : mentorizacion.correo, 
					comentario : mentorizacion.comentario, puntuacion : mentorizacion.puntuacion, fechafin : 0}).then(
					function sucessCallback(response) {
						console.log(response.data);
						index = $scope.mentorizaciones.indexOf(mentorizacion);
						$scope.mentorizaciones.splice(index, 1);
						if($scope.mentorizaciones.length == 0){
							$scope.sinresultados = true;
						}
						alert("La mentorizacion se ha cerrado con exito");
						
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						mentorizacion.enAccion = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)
		}

	}


});

appConsumer.controller("puntuarController", function($scope, $http) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargandoBusqueda = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.enAccion = false;

	$scope.activarMentorizaciones = function() {
		
		if ($scope.mostrarMentorizaciones) {
			$scope.mostrarMentorizaciones = false
			yaObtenidas = true;
			$scope.detenActualizacion();
		}
		else {
			$scope.enAccion = true;
			$scope.mostrarMentorizaciones = true;
			if (!yaObtenidas) {
				$scope.cargandoBusqueda = true;
				console.log("Consulta lanzada")
				$http.get("/mentorizado/mentorizaciones/porpuntuar").then(
					function sucessCallback(response) {
						lastload = Date.now();
						if (response.status == 200) {
							console.log(response.data);
							$scope.mentorizaciones = response.data;
							for (var i = 0; i < response.data.length; i++) {
								$scope.mentorizaciones[i].expandido = false;
								$scope.mentorizaciones[i].enAccion = false;
								$scope.mentorizaciones[i].cerrar = false;
								$scope.mentorizaciones[i].comentario = "";
								$scope.mentorizaciones[i].puntuacion = null;
							}

						} else if (response.status == 204){
							$scope.sinresultados = true;
							$scope.mentorizaciones = [];
						}
						$scope.id = setInterval(() => {
							actualizar();
						}, 60000);
						$scope.cargandoBusqueda = false;
						$scope.enAccion = false;
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						$scope.cargandoBusqueda = false;
						$scope.enAccion = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)
			}
			else{
				actualizar();
				$scope.enAccion = false;
			}
		}
	}

	var actualizar = function() {
		$http.get("/mentorizado/mentorizaciones/porpuntuar/"+lastload).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					lastload = Date.now();
					$scope.sinresultados = false;
					console.log(response.data);
					for (var i = 0; i < response.data.length; i++) {
						$scope.mentorizaciones.push(response.data[i]); 
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				$scope.detenActualizacion();
			}
		)
	}

	$scope.detenActualizacion = function() {
		if ($scope.id) {
			clearInterval(this.id);
		}
	}

	$scope.$on("$destroy", function() {
		$scope.detenActualizacion();
	});



	$scope.plegarMentorizacion = function(mentorizacion) {
		if(mentorizacion.expandido) mentorizacion.expandido = false;
		else mentorizacion.expandido = true;
		
	}
	
	$scope.abrirPuntuar = function(mentorizacion) {
		if(mentorizacion.cerrar) mentorizacion.cerrar = false;
		else mentorizacion.cerrar = true;
		
	}


	$scope.puntuarMentorizacion = function(mentorizacion, form) {
		console.log($scope)
		console.log(form.$valid)
		if(form.$valid){
				mentorizacion.enAccion = true;
				console.log("Consulta lanzada")
				$http.post("/mentorizado/mentorizaciones/puntuar", {mentor : mentorizacion.correo, 
					comentario : mentorizacion.comentario, puntuacion : mentorizacion.puntuacion, fechafin : mentorizacion.fecha_fin}).then(
					function sucessCallback(response) {
						console.log(response.data);
						index = $scope.mentorizaciones.indexOf(mentorizacion);
						$scope.mentorizaciones.splice(index, 1);
						if($scope.mentorizaciones.length == 0){
							$scope.sinresultados = true;
						}
						alert("La mentorizacion se ha puntuado con exito");
						
					},
					function errorCallback(response) {
						console.log("Fallo al acceder")
						console.log(response)
						mentorizacion.enAccion = false;
						//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
					}
				)
		}
		else{
			alert("Por favor, introduzca datos correctos")
		}

	}


});