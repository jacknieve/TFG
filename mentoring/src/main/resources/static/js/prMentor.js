

appConsumer.controller("mentorMentorizacionController", function($scope, $http, $rootScope, $window, $notification) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito
	$scope.infoFases = false;


	var inciarObtenerMentorizaciones = function() {
		$scope.cargando = true;
		$http.get("/mentor/mentorizaciones/").then(
			function sucessCallback(response) {
				lastload = Date.now();
				$scope.errorBusqueda = false;
				if (response.status == 200) {
					$scope.mentorizaciones = response.data;
					for (var i = 0; i < response.data.length; i++) {
						$scope.mentorizaciones[i].expandido = false;
						$scope.mentorizaciones[i].aceptarcerrar = false;
						if ($scope.mentorizaciones[i].uperfil.ficheros.length == 0) {
							$scope.mentorizaciones[i].sinficheros = true;
						}
						else {
							$scope.mentorizaciones[i].sinficheros = false;
						}
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

				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones,, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener las mentorizaciones, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las mentorizaciones" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
				}
				$scope.cargando = false;
				$scope.errorBusqueda = true;
				errorSound();
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
			if (!yaObtenidas || $scope.sinresultados) {
				inciarObtenerMentorizaciones();
			}
			else {
				actualizar();
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
					for (var i = 0; i < response.data.length; i++) {
						var todelete = [];
						if (response.data[i].uperfil == null) {

							todelete.push(response.data[i].correo);
						}
						else {
							response.data[i].expandido = false;
							response.data[i].aceptarcerrar = false;
							if (response.data[i].uperfil.ficheros.length == 0) {
								response.data[i].sinficheros = true;
							}
							else {
								response.data[i].sinficheros = false;
							}
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
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para traer las posibles nuevas mentorizaciones," +
							" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
							+ new Date(), null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones para acceder a las nuevas, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar traer las posibles nuevas mentorizaciones, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar traer las posibles nuevas mentorizaciones" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
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
		$http.post("/mentor/mentorizaciones/cerrar", mentorizacion.correo).then(
			function sucessCallback(response) {
				index = $scope.mentorizaciones.indexOf(mentorizacion);
				$scope.mentorizaciones.splice(index, 1);
				if ($scope.mentorizaciones.length == 0) {
					$scope.sinresultados = true;
				}
				$rootScope.popUpAbierto = false;
				$notification.success("Mentorización cerrada", "La mentorización se ha cerrado de forma exitosa", null, false);
				$scope.cargando = false;
			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, " +
							"por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la peticion", "La petición relizada al servidor no era correcta, posiblemente debido a" +
							" que alguno de los campos estaba vacío o tenía un valor incorrecto.", null, false);
						break;
					default:
						$notification.error(response.data.titulo, response.data.mensaje, null, false);
						break;
				}
				$scope.cargando = false;
				errorSound();
			}
		)

	}


	$scope.aceptarCambioFase = function(mentorizacion) {
		$scope.cargando = true;
		$http.post("/mentor/mentorizaciones/cambiarfase", { correo: mentorizacion.correo, fase: mentorizacion.fase }).then(
			function sucessCallback(response) {
				$scope.cargando = false;
				$notification.success("Fase cambiada", "La fase actual se ha cambiado de forma exitosa", null, false);

			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, " +
							"por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la peticion", "La petición relizada al servidor no era correcta, posiblemente debido a" +
							" que alguno de los campos estaba vacío o tenía un valor incorrecto.", null, false);
						break;
					default:
						$notification.error(response.data.titulo, response.data.mensaje, null, false);
						break;
				}
				$scope.cargando = false;
				errorSound();
			}
		)

	}


	$scope.verInfoFases = function() {
		if ($scope.infoFases) $scope.infoFases = false;
		else $scope.infoFases = true;
	}

	$scope.redirijirChat = function(mentorizado) {
		$scope.cargando = true;
		$http.post("/chat/idchat", mentorizado).then(
			function sucessCallback(response) {
				$window.location.href = '/chat?s=' + response.data;

			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para obtener el chat abierto con este mentorizado," +
							" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
							+ new Date(), null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene el chat abierto con este mentorizado, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener el chat abierto con este mentorizado, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					case 401:
						$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
						break;
					case 404:
						$notification.error("Sin chat", "En el servidor no hay constancia de un chat abierto con este mentorizado" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener el chat abierto con este mentorizado" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
				}
				$scope.cargando = false;
				errorSound();
			}
		)

	}

	$scope.dowloadFile = function(file, mentorizado) {
		$scope.cargando = true;

		path = "/file/download/mentorizados/" + mentorizado + "/" + file;
		window.open(path, '_blank', '');

		$scope.cargando = false;

	}

});


appConsumer.controller("peticionController", function($scope, $http, $notification) {

	$scope.mostrarPeticiones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	$scope.enError = false;
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito

	var inciarObtenerPeticiones = function() {
		$scope.cargando = true;
		$http.get("/mentor/peticiones/").then(
			function sucessCallback(response) {
				$scope.errorBusqueda = false;
				$scope.peticiones = response.data;
				if (response.data.length > 0) {
					$scope.sinresultados = false;
					for (var i = 0; i < response.data.length; i++) {
						$scope.peticiones[i].expandido = false;
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
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las solicitudes, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener las solicitudes, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las solicitudes" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
				}
				$scope.errorBusqueda = true;
				$scope.cargando = false;
				errorSound();
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
			if (!yaObtenidas || $scope.sinresultados) {
				inciarObtenerPeticiones();
			}
			else {
				actualizar();
				$scope.id = setInterval(() => {
					actualizar();
				}, 60000);
			}
		}
	}

	var actualizar = function() {
		$http.get("/mentor/peticiones/actualizar").then(
			function sucessCallback(response) {
				$scope.errorActualizar = false;
				if (response.status == 200) {
					$scope.sinresultados = false;
					for (var i = 0; i < response.data.length; i++) {
						response.data[i].expandido = false;
						$scope.peticiones.push(response.data[i]);
					}
					if ($scope.peticiones.length == 0) {
						$scope.sinresultados = true;
					}
				}

			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las solicitudes para obtener las nuevas, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener las solicitudes nuevas, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las solicitudes nuevas" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
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

	$scope.obtenerPerfilPeticion = function(peticion) {
		peticion.expandido = true;
	}

	$scope.aceptarPeticion = function(peticion) {
		$scope.cargando = true;
		$http.post("/mentor/peticiones/aceptar", peticion.info.correo).then(
			function sucessCallback(response) {
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
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, " +
							"por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la peticion", "La petición relizada al servidor no era correcta, posiblemente debido a" +
							" que alguno de los campos estaba vacío o tenía un valor incorrecto.", null, false);
						break;
					default:
						$notification.error(response.data.titulo, response.data.mensaje, null, false);
						break;
				}
				errorSound();
				$scope.cargando = false;
			}
		)

	}

	$scope.rechazarPeticion = function(peticion) {
		$scope.cargando = true;
		$http.post("/mentor/peticiones/rechazar", peticion.info.correo).then(
			function sucessCallback(response) {
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
				peticion.error = true;
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, " +
							"por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la peticion", "La petición relizada al servidor no era correcta, posiblemente debido a" +
							" que alguno de los campos estaba vacío o tenía un valor incorrecto.", null, false);
						break;
					default:
						$notification.error(response.data.titulo, response.data.mensaje, null, false);
						break;
				}
				errorSound();
				$scope.cargando = false;
			}
		)
	}



	$scope.plegarPeticion = function(peticion) {
		peticion.expandido = false;
	}





});