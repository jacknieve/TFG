var appChat = angular.module('appChat', ['notifications']);
//Notificaciones : https://github.com/swimlane/angular-notifications
appChat.config(function($httpProvider) {
	//Enable cross domain calls
	$httpProvider.defaults.useXDomain = true;
});

appChat.controller("chatController", function($scope, $http, $location, $notification) {
	$scope.mostrarChat = false;
	$scope.sinMensajes = false;
	$scope.sinSalas = false;
	$scope.cargando = false;
	$scope.mensajes = [];
	var stompClient = null;
	$scope.cuerpoMensaje = "";
	$scope.salas = [];
	$scope.errorContactos = false;
	$scope.errorMensajes = false;
	const mapaSalas = new Map();
	var soyMentor = null;
	var miUsername = "";
	$scope.salaActual = {};
	var snd = new Audio("../sounds/beep-5.wav");

	function errorSound() {
		snd.play();
	}

	$scope.recuperarChats = function() {
		$scope.cargando = true;
		$http.get("/chat/chatsuser").then(
			function sucessCallback(response) {
				$scope.errorContactos = false;
				if (response.status == 200) {
					console.log(response.data);
					$scope.sinSalas = false;
					$scope.salas = response.data;
					//Las guardamos en una coleccion para que al recibir un nuevo mensaje, se pueda administrar de forma directa
					for (var i = 0; i < $scope.salas.length; i++) {
						$scope.salas[i].abierta = false;
						mapaSalas.set($scope.salas[i].id, $scope.salas[i]);
					}

					const queryString = window.location.search;
					const urlParams = new URLSearchParams(queryString);
					const mentorParam = urlParams.get('s');
					console.log(mentorParam);
					const sala = $scope.salas.find(element => element.id == mentorParam);
					console.log(sala);
					if (sala != null) {
						$scope.abrirChat(sala);
					}

				} else if (response.status == 204) {
					$scope.sinSalas = true;
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
				//Aqui falta gestionar errores
				if (response.status == 503) {
					$notification.error("Fallo al acceder al servidor", "Se ha producido un fallo al intentar acceder al repositorio que contiene sus contactos, por favor" +
						"vuelva a intentarlo más tarde", null, false);
				}
				else if (response.status == 500) {
					$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener sus contactos, si recibe este error, por favor, pongase en contacto con "
						+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				else if (response.status == 401) {
					$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
				}
				else if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else {
					$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener sus contactos" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
				}
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
				errorSound();
				$scope.cargando = false;
				$scope.errorContactos = true;
			}
		)
	}

	var controladorMensajes = function(mensaje) {
		var mensaje = JSON.parse(mensaje.body);
		console.log(mensaje);
		//$scope.mensajes.push(mensaje);
		switch (mensaje.asunto) {
			case "MENSAJE":
				if ($scope.mostrarChat && mensaje.cuerpo.sala == $scope.salaActual.id) {
					$scope.mensajes.push(mensaje.cuerpo);

				}
				else {
					//console.log(mapaSalas);
					//console.log($scope.salaActual);
					if (mapaSalas.contains(mensaje.cuerpo.sala)) {
						mapaSalas.get(mensaje.cuerpo.sala).nuevos = true;
					}
				}
				break;
			case "CONTACTO":
				if (mensaje.cuerpo.cerrada) {
					if ($scope.mostrarChat && mensaje.cuerpo.otroUsuario == $scope.salaActual.otroUsuario) {
						$scope.salaActual.cerrada = true;

					}
					else {
						if (!$scope.sinsalas) {
							const sala = $scope.salas.find(element => element.otroUsuario == mensaje.cuerpo.otroUsuario);
							mapaSalas.get(sala.id).cerrada = true;
						}

					}
				}
				else {
					mensaje.cuerpo.abierta = false;
					$scope.salas.push(mensaje.cuerpo);
					mapaSalas.set(mensaje.cuerpo.id, mensaje.cuerpo);
					$scope.sinSalas = false;
				}
				break;
			case "NOTIFICACION":
				$notification.info(mensaje.cuerpo.titulo, "", null, false);
				break;
			case "ERROR":
				$notification.error(mensaje.cuerpo.titulo, mensaje.cuerpo.descripcion, null, false);
				break;
		}


		$scope.$apply();
	}


	var obtenerMiInfo = function() {
		$http.get("/user/miinfo").then(
			function sucessCallback(response) {
				console.log(response.data);
				soyMentor = response.data.mentor;
				miUsername = response.data.username;
				$scope.recuperarChats();
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
				$scope.errorContactos = true;
			}
		)
	}

	obtenerMiInfo();
	


	$scope.abrirChat = function(sala) {
		if (soyMentor != null) {
			$scope.cargando = true;
			$scope.mostrarChat = true;
			sala.abierta = true;
			sala.nuevos = false;
			if ($scope.salaActual != null) {
				$scope.salaActual.abierta = false;
			}
			$scope.salaActual = sala;
			$scope.cuerpoMensaje = "";
			$http.post("/chat/mensajes", { id: sala.id, mentor: soyMentor }).then(
				function sucessCallback(response) {
					$scope.errorMensajes = false;
					if (response.status == 200) {
						$scope.sinMensajes = false;
						console.log(response.data);
						$scope.mensajes = response.data;
						/*$location.hash('kk');
						$anchorScroll();*/
					} else if (response.status == 204) {
						$scope.sinMensajes = true;
						$scope.mensajes = [];
					}
					$scope.cargando = false;
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					if (response.status == 503) {
						$notification.error("Fallo al acceder al servidor", "Se ha producido un fallo al intentar acceder al repositorio que contiene los mensajes de este chat, por favor" +
							"vuelva a intentarlo más tarde", null, false);
					}
					else if (response.status == 500) {
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener la lista de mensajes de este chat, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
					}
					else if (response.status == 400) {
						$notification.error("Fallo al realizar la petición", "Se ha producido un fallo en la petición al servidor para obtener la lista de mensajes de este chat" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
					}
					else if (response.status == 401) {
						$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
					}
					else if (response.status == 0) {
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
					}
					else {
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la lista de mensajes de este chat" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
					}
					$scope.cargando = false;
					$scope.errorMensajes = true;
					errorSound();
				}
			)
		}
	}

	$scope.posicionMensaje = function(m) {
		if (soyMentor == m.deMentor) {
			return 'mensajeUser';
		} else {
			return 'mensajeOtro';
		}
	}


	$scope.enviarMensaje = function() {
		stompClient.send("/app/send", {}, JSON.stringify({ contenido: $scope.cuerpoMensaje, emisor: miUsername, receptor: $scope.salaActual.otroUsuario, deMentor: soyMentor, id: $scope.salaActual.id }));
		$scope.mensajes.push({ contenido: $scope.cuerpoMensaje, deMentor: soyMentor, sala: $scope.salaActual.id });
		$scope.cuerpoMensaje = "";
	}








	/*

	$scope.abrirChatMentor = function(otroUsuario) {
		console.log("kk")
		$rootScope.popUpAbierto = true;
		$scope.mostrarChat = true;
		otroUser = otroUsuario;
		$http.get("/messages/mentor/" + otroUsuario).then(
			function sucessCallback(response) {
				console.log(response.data);
				$scope.mensajes = response.data.mensajes;
				idChat = response.data.id;
				var socket = new SockJS('/websocket');
				stompClient = Stomp.over(socket);
				stompClient.connect({}, function(frame) {
					console.log('Connected: ' + frame);
					stompClient.subscribe("/usuario/" + idChat + "/queue/messages", function(mensaje) {
						$scope.mensajes.push(JSON.parse(mensaje.body));
					});
				});

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)

	}

	$scope.abrirChatMentorizado = function(otroUsuario) {
		console.log("kk")
		$rootScope.popUpAbierto = true;
		$scope.mostrarChat = true;
		otroUser = otroUsuario;
		$http.get("/messages/mentorizado/" + otroUsuario).then(
			function sucessCallback(response) {
				console.log(response.data);
				$scope.mensajes = response.data.mensajes;
				idChat = response.data.id;
				var socket = new SockJS('/websocket');
				stompClient = Stomp.over(socket);
				stompClient.connect({}, function(frame) {
					console.log('Connected: ' + frame);
					stompClient.subscribe("/usuario/" + idChat + "/queue/messages", function(mensaje) {
						$scope.mensajes.push(JSON.parse(mensaje.body));
					});
				});

			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				//Aqui tambien faltaria algo como para mostrar error y activar un boton de recargar
			}
		)
	}*/





	/*$scope.cerrarChat = function(){
		$rootScope.popUpAbierto = false;
		$scope.mostrarChat = false;
		idChat = 0;
		$scope.mensajes = [];
		stompClient.disconnect();
		otroUser = "";
		$scope.cuerpoMensaje = "";
		console.log("Disconnected");
	}*/

	$scope.$on("$destroy", function() {
		if (stompClient != null) {
			stompClient.disconnect();
		}
	});

});