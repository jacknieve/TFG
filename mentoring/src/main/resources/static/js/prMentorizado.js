
appConsumer.controller("mentorizadoMentorizacionController", function($scope, $http, $rootScope, $window, $notification) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito

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
						if($scope.mentorizaciones[i].uperfil.ficheros.length == 0){
							$scope.mentorizaciones[i].sinficheros = true;
						}
						else{
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
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones, por favor" +
						"vuelva a intentarlo más tarde", null, false);
				}
				else if (response.status == 500) {
					$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener las mentorizaciones, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
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
							if(response.data[i].uperfil.ficheros.length == 0){
								response.data[i].sinficheros = true;
							}
							else{
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
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones para acceder a las nuevas, por favor" +
						"vuelva a intentarlo más tarde", null, false);
				}
				else if (response.status == 500) {
					$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar traer las posibles nuevas mentorizaciones, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 400) {
					$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para traer las posibles nuevas mentorizaciones," +
						" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
						+ new Date(), null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar traer las posibles nuevas mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
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
				$rootScope.popUpAbierto = false;
				//mentorizacion.aceptarcerrar=false;
				$notification.success("Mentorización", "La mentorización se ha cerrado de forma exitosa", null, false);

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else $notification.error(response.data.titulo, response.data.mensaje, null, false);
				$scope.cargando = false;
				errorSound();
			}
		)


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
					$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene el chat abierto con este mentor, por favor" +
						"vuelva a intentarlo más tarde", null, false);
				}
				else if (response.status == 500) {
					$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener el chat abierto con este mentor, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 400) {
					$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para obtener el chat abierto con este mentor," +
						" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
						+ new Date(), null, false);
				}
				else if (response.status == 401) {
					$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
				}
				else if (response.status == 404) {
					$notification.error("Sin chat", "En el servidor no hay constancia de un chat abierto con este mentor" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener el chat abierto con este mentor" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				$scope.cargando = false;
				errorSound();
			}
		)

	}
	
	$scope.dowloadFile = function(file, mentor) {
		$scope.cargando = true;
		
		$window.location.href = "/file/download/mentor/" + mentor + "/" + file;

		$scope.cargando = false;

	}



});

appConsumer.controller("puntuarController", function($scope, $http, $rootScope, $notification) {

	$scope.mostrarMentorizaciones = false;
	$scope.sinresultados = false;
	$scope.cargando = false;
	var yaObtenidas = false;
	var lastload = new Date();
	$scope.errorBusqueda = false; //Esto nos sirve para que si falla la busqueda inicial, al plegar y volver a desplegar se pueda volver a intentar
	$scope.errorActualizar = false; //Esto nos sirve para si falla la actualización, mostrar un mesajito


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
					$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las mentorizaciones por puntuar, por favor" +
						"vuelva a intentarlo más tarde", null, false);
				}
				else if (response.status == 500) {
					$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener las mentorizaciones por puntuar, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las mentorizaciones por puntuar" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
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
			yaObtenidas = true;
			$scope.detenActualizacion();
		}
		else {
			$scope.mostrarMentorizaciones = true;
			if (!yaObtenidas || $scope.sinresultados) {
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
					$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene" +
						" las mentorizaciones para acceder a las nuevas por puntuar, por favor" +
						"vuelva a intentarlo más tarde", null, false);
				}
				else if (response.status == 500) {
					$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar traer las posibles nuevas" +
						" mentorizaciones por puntuar, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 400) {
					$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para traer las posibles nuevas mentorizaciones por puntuar," +
						" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
						+ new Date(), null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status
						+ " al intentar traer las posibles nuevas mentorizaciones por puntuar" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
						+ new Date(), null, false);
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
					$rootScope.popUpAbierto = false;
					$notification.success("Mentorización puntuada", "La mentorización se ha cerrado de forma exitosa", null, false);

				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					if (response.status == 0) {
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
					}
					else $notification.error(response.data.titulo, response.data.mensaje, null, false);
					$scope.cargando = false;
					errorSound();
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
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else $notification.error(response.data.titulo, response.data.mensaje, null, false);
				$scope.cargando = false;
				errorSound();
			}
		)

	}



});

appConsumer.controller("busquedaController", function($scope, $http, $rootScope, $notification) {

	$scope.mostrarBusqueda = false;
	$scope.sinresultados = false;
	$scope.areaseleccioanda = "sin";
	$scope.institucionseleccionada = "sin";
	$scope.horasmes = 4;
	$scope.cargando = false;
	$scope.errorBusqueda = false;

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
					$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene los mentores, por favor" +
						"vuelva a intentarlo más tarde", null, false);
				}
				else if (response.status == 500) {
					$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener los mentores, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 400) {
					$notification.error("Fallo en la solicitud", "Se ha producido un fallo con alguno de los parámetros de la búsqueda, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener los mentores" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				errorSound();
				$scope.errorBusqueda = true;
				$scope.cargando = false;
			}
		)
	}

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
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio para obtener la información del mentor, por favor" +
							"vuelva a intentarlo más tarde", null, false);
					}
					else if (response.status == 500) {
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener la información del mentor, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
					}
					else if (response.status == 400) {
						$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para obtener la información del mentor," +
							" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
							+ new Date(), null, false);
					}
					else if (response.status == 0) {
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
					}
					else {
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la información del mentor" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
					}
					errorSound();
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
				$rootScope.popUpAbierto = false;
				$notification.success("Solicitud enviada", response.data.titulo, null, false);
				/*mentor.expandido = true;
				mentor.obtenido = true;
				mentor.info = response.data;*/

			},
			function errorCallback(response) {
				if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error(response.data.titulo, response.data.mensaje, null, false);
				}
				console.log("Fallo al acceder")
				console.log(response)
				errorSound();
				mentor.cargando = false;
			}
		)
	}


});