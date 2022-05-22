var appConsumer = angular.module('appConsumer', ['ngAnimate', 'notifications']);


appConsumer.config(function($httpProvider) {
	//Enable cross domain calls
	$httpProvider.defaults.useXDomain = true;
});
appConsumer.controller("globalController", function($rootScope, $scope, $http) {
	$rootScope.popUpAbierto = false;



});

appConsumer.controller("busquedaController", function($scope, $http, $rootScope) {

	$scope.mostrarBusqueda = false;
	$scope.sinresultados = false;
	$scope.areaseleccioanda = "sin";
	$scope.institucionseleccionada = "sin";
	$scope.horasmes = 4;
	$scope.cargando = false;
	$scope.enError = false;
	$scope.mensajeError = "";
	$scope.errorBusqueda = false;
	$scope.enAcierto = false;

	$scope.activarBusqueda = function() {
		if ($scope.mostrarBusqueda) $scope.mostrarBusqueda = false
		else $scope.mostrarBusqueda = true;
	}

	$scope.buscar = function() {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		console.log(typeof ($scope.horasmes));
		if ($scope.horasmes == null) $scope.horasmes = 4;
		$http.post("/mentorizado/busqueda", { area: $scope.areaseleccioanda, institucion: $scope.institucionseleccionada, horas: $scope.horasmes }).then(
			function sucessCallback(response) {
				//Si la peticion tiene los Path variables mal, o no es correcto, suelta un 400, y si el ultimo es vacio, suelta un 404
				$scope.errorBusqueda = false;
				if (response.status == 200) {
					console.log(response.data);
					$scope.sinresultados = false;
					$scope.mentores = response.data;
					for (var i = 0; i < response.data.length; i++) {
						$scope.mentores[i].expandido = false;
						$scope.mentores[i].obtenido = false;
						$scope.mentores[i].cargando = false;
						$scope.mentores[i].solicitud = false;
						$scope.mentores[i].motivo = "";
					}

				} else if (response.status == 204) {
					$scope.mentores = [];
					$scope.sinresultados = true;
				}
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log(response)
				console.log("Fallo al acceder")
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene los mentores, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar obtener los mentores, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 400) {
					abrirError("Se ha producido un fallo con alguno de los parámetros de la búsqueda, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener los mentores" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.cargando = false;
				$scope.errorBusqueda = true;
				$scope.cargando = false;
			}
		)
	}


	/*$scope.buscardos = function(){
		$scope.cargando = true;
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
				
				$scope.cargando = false;
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
			mentor.cargando = true;
			console.log("Consulta lanzada")
			$http.post("/mentorizado/obtenermentor", mentor.correo).then(
				function sucessCallback(response) {
					console.log(response.data);
					mentor.expandido = true;
					mentor.obtenido = true;
					mentor.info = response.data;
					mentor.cargando = false;
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					if (response.status == 503) {
						abrirError("Se ha producido un fallo al intentar acceder al repositorio para obtener la información del mentor, por favor" +
							"vuelva a intentarlo más tarde");
					}
					else if (response.status == 500) {
						abrirError("Se ha producido un fallo interno en el servidor al intentar obtener obtener la información del mentor " +
							", si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
					}
					else if (response.status == 400) {
						abrirError("Se ha producido un fallo en la petición al servidor para obtener la información del mentor" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
					}
					else if (response.status == 0) {
						abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
					}
					else {
						abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la información del mentor" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
					}
					mentor.cargando = false;
				}
			)
		}
	}

	$scope.plegarMentor = function(mentor) {
		mentor.expandido = false;
	}

	$scope.mostrarOcultarSolicitud = function(mentor) {
		if (mentor.solicitud) {
			mentor.solicitud = false
			$rootScope.popUpAbierto = false;
		}
		else {
			mentor.solicitud = true;
			$rootScope.popUpAbierto = true;
		}
	}


	$scope.enviarSolicitud = function(mentor) {
		mentor.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/mentorizado/enviarsolicitud", { mentor: mentor.correo, motivo: mentor.motivo }).then(
			function sucessCallback(response) {
				console.log(response.data);
				mentor.cargando = false;
				mentor.solicitud = false;
				$scope.enAcierto = true;
				/*mentor.expandido = true;
				mentor.obtenido = true;
				mentor.info = response.data;*/

			},
			function errorCallback(response) {
				/*if (response.status == 409) {
					alert("Ya has establecido una relacion de mentorizacion con este mentor");
				}*/
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError(response.data.mensaje);
				}
				console.log("Fallo al acceder")
				console.log(response)

				mentor.cargando = false;
			}
		)
	}


	var abrirError = function(mensaje) {
		$scope.enError = true;
		$scope.mensajeError = mensaje;
		$rootScope.popUpAbierto = true;
	}

	$scope.cerrarError = function() {
		$scope.enError = false;
		$scope.mensajeError = "";
		$rootScope.popUpAbierto = false;
	}

	$scope.cerrarAcierto = function() {
		$scope.enAcierto = false;
		$rootScope.popUpAbierto = false;
	}

});

appConsumer.controller("peticionController", function($scope, $http, $rootScope) {

	$scope.mostrarPeticiones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	$scope.enError = false;
	$scope.mensajeError = "";
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito

	var inciarObtenerPeticiones = function() {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/mentor/peticiones/").then(
			function sucessCallback(response) {
				$scope.errorBusqueda = false;
				console.log(response.data);
				$scope.peticiones = response.data;
				if (response.data.length > 0) {
					for (var i = 0; i < response.data.length; i++) {
						$scope.peticiones[i].expandido = false;
						$scope.peticiones[i].obtenido = false;
						$scope.peticiones[i].cargando = false;
					}

				} else {
					$scope.peticiones = [];
					$scope.sinresultados = true;
				}
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las solicitudes, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar obtener las solicitudes, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las solicitudes" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.errorBusqueda = true;
				$scope.cargando = false;
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)
	}

	$scope.activarPeticiones = function() {
		if ($scope.mostrarPeticiones) {
			$scope.mostrarPeticiones = false
			if (!$scope.errorBusqueda) yaObtenidas = true;
			$scope.errorBusqueda = false;
			$scope.errorActualizar = false;
			$scope.detenActualizacion();
		}
		else {
			$scope.mostrarPeticiones = true;
			if (!yaObtenidas) {
				inciarObtenerPeticiones();
			}
			else {
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
			}
		}
	}

	var actualizar = function() {
		console.log("actualizando peticiones");
		$http.get("/mentor/peticiones/actualizar").then(
			function sucessCallback(response) {
				$scope.errorActualizar = false;
				if (response.status == 200) {
					console.log(response.data);
					$scope.sinresultados = false;
					for (var i = 0; i < response.data.length; i++) {
						response.data[i].expandido = false;
						response.data[i].obtenido = false;
						response.data[i].cargando = false;
						$scope.peticiones.push(response.data[i]);
					}
					if ($scope.peticiones.length == 0) {
						$scope.sinresultados = true;
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las solicitudes para obtener las nuevas, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar obtener las solicitudes nuevas, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las solicitudes nuevas" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.errorActualizar = true;
				$scope.detenActualizacion();
			}
		)
	}

	$scope.detenActualizacion = function() {
		if ($scope.id) {
			clearInterval(this.id);
			console.log("Intervalo detenido")
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
			$scope.cargando = true;
			console.log("Consulta lanzada")
			$http.post("/mentor/peticiones/perfil", peticion.mentorizado).then(
				function sucessCallback(response) {
					console.log(response.data);
					peticion.expandido = true;
					peticion.obtenido = true;
					peticion.info = response.data;
					$scope.cargando = false;
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					if (response.status == 503) {
						abrirError("Se ha producido un fallo al intentar acceder al repositorio para obtener la información del mentorizado que envió la solicitud, por favor" +
							"vuelva a intentarlo más tarde");
					}
					else if (response.status == 500) {
						abrirError("Se ha producido un fallo interno en el servidor al intentar obtener obtener la información del mentorizado que " +
							"envió la solicitud, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
					}
					else if (response.status == 400) {
						abrirError("Se ha producido un fallo en la petición al servidor para obtener la información del mentorizado que envió la solicitud" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
					}
					else if (response.status == 0) {
						abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
					}
					else {
						abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la información del mentorizado que envió la solicitud" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
					}
					$scope.cargando = false;
				}
			)
		}
	}

	$scope.aceptarPeticion = function(peticion) {
		$scope.cargando = true;
		peticion.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/mentor/peticiones/aceptar", peticion.mentorizado).then(
			function sucessCallback(response) {
				console.log(response);
				if (response.status == 200) {
					index = $scope.peticiones.indexOf(peticion);
					$scope.peticiones.splice(index, 1);
					if ($scope.peticiones.length == 0) {
						$scope.sinresultados = true;
					}
				}
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else abrirError(response.data.mensaje);
				$scope.cargando = false;
				peticion.cargando = false;
			}
		)

	}

	$scope.rechazarPeticion = function(peticion) {
		$scope.cargando = true;
		peticion.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/mentor/peticiones/rechazar", peticion.mentorizado).then(
			function sucessCallback(response) {
				console.log(response);
				if (response.status == 200) {
					index = $scope.peticiones.indexOf(peticion);
					$scope.peticiones.splice(index, 1);
					if ($scope.peticiones.length == 0) {
						$scope.sinresultados = true;
					}

				}
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				peticion.error = true;
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else abrirError(response.data.mensaje);
				$scope.cargando = false;
				peticion.cargando = false;
			}
		)
	}



	$scope.plegarPeticion = function(peticion) {
		peticion.expandido = false;
	}

	var abrirError = function(mensaje) {
		$scope.enError = true;
		$scope.mensajeError = mensaje;
		$rootScope.popUpAbierto = true;
	}

	$scope.cerrarError = function() {
		$scope.enError = false;
		$scope.mensajeError = "";
		$rootScope.popUpAbierto = false;
	}





});

appConsumer.controller("notificacionController", function($scope, $http, $notification) {

	$scope.mostrarsin = false;
	$scope.cargando = false;
	$scope.enfallo = false;
	$scope.mensajeError = "";
	var stompClient = null;
	var miUsername = "";

	
	var obtenerMiInfo = function() {
		$http.get("/user/miinfo").then(
			function sucessCallback(response) {
				console.log(response.data);
				miUsername = response.data.username;
				$scope.iniciaNotificaciones();
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 401) {
					$notification.error("Sin autorización", "No tienes permiso para obtener la información de tu cuenta en esta pantalla", null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la información de tu cuenta para relaizar las acciones del chat" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				errorSound();
				$scope.enfallo = true;
			}
		)
	}

	$scope.iniciaNotificaciones = function() {
		$scope.cargando = false;
		console.log("Consulta lanzada")
		$http.get("/user/notificaciones").then(
			function sucessCallback(response) {
				$scope.enfallo = false;
				if (response.status == 200) {
					console.log(response.data);
					$scope.notificaciones = response.data;
					$scope.cargando = false;
				}
				else if (response.status == 204) {
					$scope.notificaciones = [];
					$scope.mostrarsin = true;
				}
					var socket = new SockJS('/websocket');
					stompClient = Stomp.over(socket);
					stompClient.connect({}, function(frame) {
						console.log('Connected: ' + frame);
						stompClient.subscribe("/usuario/" + miUsername + "/queue/messages", controladorMensajes);
					});
					$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				$scope.enfallo = true;
				if (response.status == 503) {
					$scope.mensajeError = "Se ha producido un fallo al intentar acceder al repositorio que contiene las notificaciones, por favor" +
						"vuelva a intentarlo más tarde";
				}
				else if (response.status == 500) {
					$scope.mensajeError = "Se ha producido un fallo interno en el servidor, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date();
				}
				else if (response.status == 0) {
					$scope.mensajeError = "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias";
				}
				else {
					$scope.mensajeError = "Se ha producido un fallo no previsto con codigo de error " + response.status +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date();
				}
				$scope.cargando = false;
			}
		)

	}
	//Llamamos a la función nada más cargar
	obtenerMiInfo();
	
	
	$scope.reiniciarActualizacion = function() {
		obtenerMiInfo();
	}


	$scope.$on("$destroy", function() {
		if (stompClient != null) {
			stompClient.disconnect();
		}
	});

	$scope.borrarNotificacion = function(notificacion) {
		$scope.cargando = true;
		$http.post("/user/notificaciones/delete", notificacion.id).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					console.log(response.data);
					index = $scope.notificaciones.indexOf(notificacion);
					$scope.notificaciones.splice(index, 1);
					if ($scope.notificaciones.length == 0) {
						$scope.mostrarsin = true;
					}
				}
				$scope.cargando = false;

			},
			function errorCallback(response) {
				console.log("Fallo al eliminar");
				console.log(response);
				$scope.enfallo = true;
				if (response.status == 0) {
					$scope.mensajeError = "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias";
				}
				else $scope.mensajeError = response.data.mensaje;
				$scope.cargando = false;
			}
		)
	}

	var controladorMensajes = function(mensaje) {
		var mensaje = JSON.parse(mensaje.body);
		console.log(mensaje);
		//$scope.mensajes.push(mensaje);
		switch (mensaje.asunto) {
			case "NOTIFICACION":
				$scope.notificaciones.unshift(mensaje.cuerpo);
				$notification.info(mensaje.cuerpo.titulo, "", null, false);
				break;
			case "ERROR":
				$notification.error(mensaje.cuerpo.titulo, mensaje.cuerpo.descripcion, null, false);
				break;
		}
		$scope.$apply();
	}




});


appConsumer.controller("mentorMentorizacionController", function($scope, $http, $rootScope, $window) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.enError = false;
	$scope.mensajeError = "";
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito
	$scope.enAcierto = false;
	$scope.infoFases = false;



	var inciarObtenerMentorizaciones = function() {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/mentor/mentorizaciones/").then(
			function sucessCallback(response) {
				lastload = Date.now();
				$scope.errorBusqueda = false;
				if (response.status == 200) {
					console.log(response.data);
					console.log(response);
					$scope.mentorizaciones = response.data;
					console.log(typeof ($scope.mentorizaciones[0].fase));
					for (var i = 0; i < response.data.length; i++) {
						$scope.mentorizaciones[i].expandido = false;
						$scope.mentorizaciones[i].aceptarcerrar = false;
					}
				} else if (response.status == 204) {
					$scope.sinresultados = true;
					$scope.mentorizaciones = [];
				}
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar obtener las mentorizaciones, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.cargando = false;
				$scope.errorBusqueda = true;
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)


	}

	$scope.activarMentorizaciones = function() {
		if ($scope.mostrarMentorizaciones) {
			$scope.mostrarMentorizaciones = false
			if (!$scope.errorBusqueda) yaObtenidas = true;
			$scope.errorBusqueda = false;
			$scope.detenActualizacion();
			$scope.errorActualizar = false;
		}
		else {
			$scope.mostrarMentorizaciones = true;
			if (!yaObtenidas) {
				inciarObtenerMentorizaciones();
			}
			else {
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
			}
		}
	}

	var actualizar = function() {
		$http.get("/mentor/mentorizaciones/actualizar/" + lastload).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					lastload = Date.now();
					$scope.errorActualizar = false;
					$scope.sinresultados = false;
					console.log(response.data);
					for (var i = 0; i < response.data.length; i++) {
						var todelete = [];
						if (response.data[i].uperfil == null) {

							todelete.push(response.data[i].correo);
						}
						else {
							response.data[i].expandido = false;
							response.data[i].aceptarcerrar = false;
							$scope.mentorizaciones.push(response.data[i]);
						}
						if (todelete.length > 0) {
							$scope.mentorizaciones = $scope.mentorizaciones.filter(function(elemento) {
								return todelete.indexOf(elemento.correo) == -1;
							});

						}
						if ($scope.mentorizaciones.length == 0) {
							$scope.sinresultados = true;
						}
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones para acceder a las nuevas, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar traer las posibles nuevas mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 400) {
					abrirError("Se ha producido un fallo en la petición al servidor para traer mentorizaciones nuevas" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las nuevas mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.errorActualizar = true;
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
		if (mentorizacion.expandido) mentorizacion.expandido = false;
		else mentorizacion.expandido = true;
	}


	$scope.confirmarCerrar = function(mentorizacion) {
		if (mentorizacion.aceptarcerrar) {
			mentorizacion.aceptarcerrar = false;
			$rootScope.popUpAbierto = false;
		}
		else {
			mentorizacion.aceptarcerrar = true;
			$rootScope.popUpAbierto = true;
		}
	}


	$scope.cerrarMentorizacion = function(mentorizacion) {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/mentor/mentorizaciones/cerrar", mentorizacion.correo).then(
			function sucessCallback(response) {
				console.log(response.data);
				index = $scope.mentorizaciones.indexOf(mentorizacion);
				$scope.mentorizaciones.splice(index, 1);
				if ($scope.mentorizaciones.length == 0) {
					$scope.sinresultados = true;
				}

				//alert("La mentorizacion se ha cerrado con exito");
				//mentorizacion.aceptarcerrar=false;
				//$rootScope.popUpAbierto = false;
				$scope.enAcierto = true;
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else abrirError(response.data.mensaje);
				$scope.cargando = false;
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)

	}


	$scope.aceptarCambioFase = function(mentorizacion) {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/mentor/mentorizaciones/cambiarfase", { correo: mentorizacion.correo, fase: mentorizacion.fase }).then(
			function sucessCallback(response) {
				console.log(response.data);
				//alert("La fase se ha cambiado con exito");
				$scope.cargando = false;

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else abrirError(response.data.mensaje);
				$scope.cargando = false;
			}
		)

	}

	var abrirError = function(mensaje) {
		$scope.enError = true;
		$scope.mensajeError = mensaje;
		$rootScope.popUpAbierto = true;
	}

	$scope.cerrarError = function() {
		$scope.enError = false;
		$scope.mensajeError = "";
		$rootScope.popUpAbierto = false;
	}

	$scope.cerrarAcierto = function() {
		$scope.enAcierto = false;
		$rootScope.popUpAbierto = false;
	}

	$scope.verInfoFases = function() {
		if ($scope.infoFases) $scope.infoFases = false;
		else $scope.infoFases = true;
	}

	$scope.redirijirChat = function(mentorizado) {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/chat/idchat", mentorizado).then(
			function sucessCallback(response) {
				console.log(response.data);
				//alert("La fase se ha cambiado con exito");
				$window.location.href = '/chat?s=' + response.data;

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene el chat abierto con este mentorizado, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar traer el id del chat con este mentorizado" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 400) {
					abrirError("Se ha producido un fallo en la petición al servidor para traer el id del chat con este mentorizado" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 401) {
					abrirError("No tienes permiso para realizar esta acción.");
				}
				else if (response.status == 404) {
					abrirError("En el servidor no hay constancia de un chat abierto con este mentorizado" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener el id del chat con este mentorizado" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.cargando = false;
			}
		)

	}

});

appConsumer.controller("mentorizadoMentorizacionController", function($scope, $http, $rootScope, $window) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.enError = false;
	$scope.mensajeError = "";
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito
	$scope.enAcierto = false;

	var inciarObtenerMentorizaciones = function() {
		$scope.errorBusqueda = false;
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/mentorizado/mentorizaciones/").then(
			function sucessCallback(response) {
				lastload = Date.now();
				$scope.errorBusqueda = false;
				if (response.status == 200) {
					console.log(response.data);
					console.log(response);
					$scope.mentorizaciones = response.data;
					console.log(typeof ($scope.mentorizaciones[0].fase));
					for (var i = 0; i < response.data.length; i++) {
						$scope.mentorizaciones[i].expandido = false;
						$scope.mentorizaciones[i].aceptarcerrar = false;
						$scope.mentorizaciones[i].cerrar = false;
						$scope.mentorizaciones[i].comentario = "";
						$scope.mentorizaciones[i].puntuacion = null;
					}
				} else if (response.status == 204) {
					$scope.sinresultados = true;
					$scope.mentorizaciones = [];
				}
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar obtener las mentorizaciones, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.cargando = false;
				$scope.errorBusqueda = true;
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)


	}


	$scope.activarMentorizaciones = function() {
		if ($scope.mostrarMentorizaciones) {
			$scope.mostrarMentorizaciones = false
			if (!$scope.errorBusqueda) yaObtenidas = true;
			$scope.errorBusqueda = false;
			$scope.detenActualizacion();
			$scope.errorActualizar = false;
		}
		else {
			$scope.mostrarMentorizaciones = true;
			if (!yaObtenidas) {
				inciarObtenerMentorizaciones();
			}
			else {
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
			}
		}
	}


	var actualizar = function() {
		$scope.errorActualizar = false;
		$http.get("/mentorizado/mentorizaciones/actualizar/" + lastload).then(
			function sucessCallback(response) {
				$scope.errorActualizar = false;
				if (response.status == 200) {
					lastload = Date.now();
					$scope.sinresultados = false;
					console.log(response.data);
					for (var i = 0; i < response.data.length; i++) {
						var todelete = [];
						if (response.data[i].uperfil == null) {

							todelete.push(response.data[i].correo);
						}
						else {
							response.data[i].cerrar = false;
							response.data[i].comentario = "";
							response.data[i].puntuacion = null;
							response.data[i].expandido = false;
							response.data[i].aceptarcerrar = false;
							$scope.mentorizaciones.push(response.data[i]);
						}
						if (todelete.length > 0) {
							$scope.mentorizaciones = $scope.mentorizaciones.filter(function(elemento) {
								return todelete.indexOf(elemento.correo) == -1;
							});

						}
						if ($scope.mentorizaciones.length == 0) {
							$scope.sinresultados = true;
						}
					}
				}

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones para acceder a las nuevas, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar traer las posibles nuevas mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 400) {
					abrirError("Se ha producido un fallo en la petición al servidor para traer mentorizaciones nuevas" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las nuevas mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.errorActualizar = true;
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
		if (mentorizacion.expandido) mentorizacion.expandido = false;
		else mentorizacion.expandido = true;

	}

	$scope.abrirPuntuar = function(mentorizacion) {
		if (mentorizacion.cerrar) {
			mentorizacion.cerrar = false;
			$rootScope.popUpAbierto = false;
		}
		else {
			mentorizacion.cerrar = true;
			$rootScope.popUpAbierto = true;
		}

	}

	$scope.confirmarCerrar = function(mentorizacion) {
		if (mentorizacion.aceptarcerrar) mentorizacion.aceptarcerrar = false;
		else mentorizacion.aceptarcerrar = true;

	}



	$scope.cerrarMentorizacion = function(mentorizacion) {

		console.log(typeof (mentorizacion.puntuacion))
		$scope.cargando = true;
		var puntuacion = -1;
		if (mentorizacion.puntuacion != null) puntuacion = mentorizacion.puntuacion;
		console.log("Consulta lanzada")
		$http.post("/mentorizado/mentorizaciones/cerrar", {
			mentor: mentorizacion.correo,
			comentario: mentorizacion.comentario, puntuacion: puntuacion, fechafin: 0
		}).then(
			function sucessCallback(response) {
				console.log(response.data);
				index = $scope.mentorizaciones.indexOf(mentorizacion);
				$scope.mentorizaciones.splice(index, 1);
				if ($scope.mentorizaciones.length == 0) {
					$scope.sinresultados = true;
				}
				$scope.cargando = false;
				//mentorizacion.aceptarcerrar=false;
				$scope.enAcierto = true;

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else abrirError(response.data.mensaje);
				$scope.cargando = false;
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)


	}

	var abrirError = function(mensaje) {
		$scope.enError = true;
		$scope.mensajeError = mensaje;
		$rootScope.popUpAbierto = true;
	}

	$scope.cerrarError = function() {
		$scope.enError = false;
		$scope.mensajeError = "";
		$rootScope.popUpAbierto = false;
	}

	$scope.cerrarAcierto = function() {
		$scope.enAcierto = false;
		$rootScope.popUpAbierto = false;
	}

	$scope.redirijirChat = function(mentor) {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/chat/idchat", mentor).then(
			function sucessCallback(response) {
				console.log(response.data);
				//alert("La fase se ha cambiado con exito");
				$window.location.href = '/chat?s=' + response.data;

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene los chats abiertos, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar traer el id del chat con este mentor" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 400) {
					abrirError("Se ha producido un fallo en la petición al servidor para traer el id del chat con este mentor" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 401) {
					abrirError("No tienes permiso para realizar esta acción.");
				}
				else if (response.status == 404) {
					abrirError("En el servidor no hay constancia de un chat abierto con este mentor" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener el id del chat con este mentor" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.cargando = false;
			}
		)

	}



});

appConsumer.controller("puntuarController", function($scope, $http, $rootScope) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.enError = false;
	$scope.mensajeError = "";
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito
	$scope.enAcierto = false;


	var inciarObtenerPorPuntuar = function() {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/mentorizado/mentorizaciones/porpuntuar").then(
			function sucessCallback(response) {
				lastload = Date.now();
				$scope.errorBusqueda = false;
				if (response.status == 200) {
					console.log(response.data);
					$scope.mentorizaciones = response.data;
					for (var i = 0; i < response.data.length; i++) {
						$scope.mentorizaciones[i].expandido = false;
						$scope.mentorizaciones[i].aceptarcerrar = false;
						$scope.mentorizaciones[i].cerrar = false;
						$scope.mentorizaciones[i].comentario = "";
						$scope.mentorizaciones[i].puntuacion = null;
					}
				} else if (response.status == 204) {
					$scope.sinresultados = true;
					$scope.mentorizaciones = [];
				}

				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones por puntuar, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar obtener las mentorizaciones por puntuar, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las mentorizaciones por puntuar" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.cargando = false;
				$scope.errorBusqueda = true;
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)


	}




	$scope.activarMentorizaciones = function() {

		if ($scope.mostrarMentorizaciones) {
			$scope.mostrarMentorizaciones = false
			yaObtenidas = true;
			$scope.detenActualizacion();
		}
		else {
			$scope.mostrarMentorizaciones = true;
			if (!yaObtenidas) {
				inciarObtenerPorPuntuar();
			}
			else {
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
			}
		}
	}

	var actualizar = function() {
		$http.get("/mentorizado/mentorizaciones/porpuntuar/" + lastload).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					lastload = Date.now();
					$scope.errorActualizar = false;
					$scope.sinresultados = false;
					console.log(response.data);
					for (var i = 0; i < response.data.length; i++) {
						response.data[i].cerrar = false;
						response.data[i].comentario = "";
						response.data[i].puntuacion = null;
						response.data[i].expandido = false;
						response.data[i].aceptarcerrar = false;
						$scope.mentorizaciones.push(response.data[i]);
						if ($scope.mentorizaciones.length == 0) {
							$scope.sinresultados = true;
						}
					}
				}
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones para acceder a las nuevas por puntuar, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar traer las posibles nuevas mentorizaciones por puntuar" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 400) {
					abrirError("Se ha producido un fallo en la petición al servidor para traer mentorizaciones por puntuar nuevas" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las nuevas mentorizaciones por puntuar" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date());
				}
				$scope.errorActualizar = true;
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
		if (mentorizacion.expandido) mentorizacion.expandido = false;
		else mentorizacion.expandido = true;

	}

	$scope.abrirPuntuar = function(mentorizacion) {
		console.log($rootScope.popUpAbierto);
		if (mentorizacion.cerrar) {
			mentorizacion.cerrar = false;
			$rootScope.popUpAbierto = false;
		}
		else {
			mentorizacion.cerrar = true;
			$rootScope.popUpAbierto = true;
		}

	}


	$scope.puntuarMentorizacion = function(mentorizacion) {
		if (mentorizacion.puntuacion != null) {
			$scope.cargando = true;
			console.log("Consulta lanzada")
			$http.post("/mentorizado/mentorizaciones/puntuar", {
				mentor: mentorizacion.correo,
				comentario: mentorizacion.comentario, puntuacion: mentorizacion.puntuacion, fechafin: mentorizacion.fecha_fin
			}).then(
				function sucessCallback(response) {
					console.log(response.data);
					index = $scope.mentorizaciones.indexOf(mentorizacion);
					$scope.mentorizaciones.splice(index, 1);
					if ($scope.mentorizaciones.length == 0) {
						$scope.sinresultados = true;
					}
					$scope.cargando = false;
					$scope.enAcierto = true;

				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					if (response.status == 0) {
						abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
					}
					else {
						abrirError(response.data.mensaje);
					}
					$scope.cargando = false;
					//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
				}
			)
		}
	}

	$scope.descartarPuntuar = function(mentorizacion) {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.post("/mentorizado/mentorizaciones/puntuar", {
			mentor: mentorizacion.correo,
			comentario: null, puntuacion: -1, fechafin: mentorizacion.fecha_fin
		}).then(
			function sucessCallback(response) {
				console.log(response.data);
				index = $scope.mentorizaciones.indexOf(mentorizacion);
				$scope.mentorizaciones.splice(index, 1);
				if ($scope.mentorizaciones.length == 0) {
					$scope.sinresultados = true;
				}
				$scope.cargando = false;

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else abrirError(response.data.mensaje);
				$scope.cargando = false;
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)

	}

	var abrirError = function(mensaje) {
		$scope.enError = true;
		$scope.mensajeError = mensaje;
		$rootScope.popUpAbierto = true;
	}

	$scope.cerrarError = function() {
		$scope.enError = false;
		$scope.mensajeError = "";
		$rootScope.popUpAbierto = false;
	}

	$scope.cerrarAcierto = function() {
		$scope.enAcierto = false;
		$rootScope.popUpAbierto = false;
	}


});
