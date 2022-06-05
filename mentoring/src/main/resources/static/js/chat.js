var appChat = angular.module('appChat', ['notifications']);
//Notificaciones : https://github.com/swimlane/angular-notifications
appChat.config(function($httpProvider) {
	//Enable cross domain calls
	$httpProvider.defaults.useXDomain = true;
});

//https://o7planning.org/11673/spring-boot-file-upload-with-angularjs
// DIRECTIVE - FILE MODEL
appChat.directive('fileModel', ['$parse', function($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function() {
				scope.$apply(function() {
					modelSetter(scope, element[0].files[0]);
				});
			});
		}
	};

}]);

appChat.controller("chatController", function($scope, $http, $notification) {
	$scope.mostrarChat = false;
	$scope.sinMensajes = false;
	$scope.sinSalas = false;
	$scope.cargando = false;
	$scope.lostconetion = false;
	$scope.ficheroEnviar = null;
	$scope.enEnvioFile = false;
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
					$scope.sinSalas = false;
					$scope.salas = response.data;
					//Las guardamos en una coleccion para que al recibir un nuevo mensaje, se pueda administrar de forma directa
					for (var i = 0; i < $scope.salas.length; i++) {
						$scope.salas[i].abierta = false;
						mapaSalas.set($scope.salas[i].id, $scope.salas[i]);
					}

					const queryString = window.location.search;
					const urlParams = new URLSearchParams(queryString);
					const salaParam = Number(urlParams.get('s'));
					if (mapaSalas.has(salaParam)) {
						$scope.abrirChat(mapaSalas.get(salaParam));
					}

				} else if (response.status == 204) {
					$scope.sinSalas = true;
				}
				var socket = new SockJS('/websocket');
				stompClient = Stomp.over(socket);
				stompClient.connect({}, function(frame) {
					//console.log('Connected: ' + frame);
					stompClient.subscribe("/usuario/" + miUsername + "/queue/messages", controladorMensajes);
				});
				socket.onclose = function() {
					console.log("Cerrado conexion");
					$notification.warning("Conexión perdida", "Se ha perdido la conexión con el servidor.", null, false);
					$scope.lostconetion = true;
					stompClient.disconnect();
					$scope.$apply();
				}
				$scope.cargando = false;

			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene sus contactos, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener sus contactos, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					case 401:
						$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener sus contactos" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
				}
				$scope.cargando = false;
				$scope.errorContactos = true;
			}
		)
	}

	var controladorMensajes = function(mensaje) {
		var mensaje = JSON.parse(mensaje.body);
		switch (mensaje.asunto) {
			case "MENSAJE":
				if ($scope.mostrarChat && mensaje.cuerpo.sala == $scope.salaActual.id) {
					$scope.mensajes.push(mensaje.cuerpo);

				}
				else {
					if (mapaSalas.has(mensaje.cuerpo.sala)) {
						mapaSalas.get(mensaje.cuerpo.sala).nuevos = true;
					}
				}
				break;
			case "CONTACTO":
				if (mensaje.cuerpo.cerrada) {
					if (!$scope.sinsalas) {
						const sala = $scope.salas.find(element => element.otroUsuario == mensaje.cuerpo.otroUsuario);
						mapaSalas.get(sala.id).cerrada = true;
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
			case "MENSAJEERROR":
				$notification.error(mensaje.cuerpo.titulo, mensaje.cuerpo.descripcion, null, false);
				$scope.mensajes.pop();
				break;
		}


		$scope.$apply();
	}


	var obtenerMiInfo = function() {
		$http.get("/user/miinfo").then(
			function sucessCallback(response) {
				soyMentor = response.data.mentor;
				miUsername = response.data.username;
				$scope.recuperarChats();
			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, " +
							"por favor, disculpen las molestias", null, false);
						break;
					case 401:
						$notification.error("Sin autorización", "No tienes permiso para obtener la información de tu cuenta en esta pantalla", null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la información de tu cuenta para relaizar las acciones del chat" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
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
						$scope.mensajes = response.data;
					} else if (response.status == 204) {
						$scope.sinMensajes = true;
						$scope.mensajes = [];
					}
					$scope.cargando = false;
				},
				function errorCallback(response) {
					switch (response.status) {
						case 0:
							$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
							break;
						case 400:
							$notification.error("Fallo al realizar la petición", "Se ha producido un fallo en la petición al servidor para obtener la lista de mensajes de este chat" +
								", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
							break;
						case 503:
							$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene los mensajes de este chat, por favor" +
								"vuelva a intentarlo más tarde", null, false);
							break;
						case 500:
							$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener la lista de mensajes de este chat, si recibe este error, por favor, pongase en contacto con "
								+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
							break;
						case 401:
							$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
							break;
						default:
							$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la lista de mensajes de este chat" +
								", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
							break;
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

	$scope.esMioMensaje = function(m) {
		if (soyMentor == m.deMentor) {
			return true;
		} else {
			return false;
		}
	}


	$scope.enviarMensaje = function() {
		if ($scope.cuerpoMensaje == "") {
			$notification.error("Mensaje vacío", "Por favor, escribe algo antes de darle a enviar.", null, false);
		}
		else {
			stompClient.send("/app/send", {}, JSON.stringify({ contenido: $scope.cuerpoMensaje, emisor: miUsername, receptor: $scope.salaActual.otroUsuario, deMentor: soyMentor, id: $scope.salaActual.id }));
			$scope.mensajes.push({ contenido: $scope.cuerpoMensaje, deMentor: soyMentor, sala: $scope.salaActual.id, deTexto: true });
			$scope.cuerpoMensaje = "";
		}
	}



	$scope.$on("$destroy", function() {
		if (stompClient != null) {
			stompClient.disconnect();
		}
	});

	$scope.cambiarEnviar = function() {
		$scope.enEnvioFile = !$scope.enEnvioFile;
	}

	$scope.enviarFichero = function() {
		if ($scope.ficheroEnviar != null) {
			const size = ($scope.ficheroEnviar.size / 1024) / 1024;
			if (size > 15.0) {
				$notification.error("Fichero demasiado grande", "El tamaño del fichero excede lo permitido (15MB)", null, false);
				errorSound();
			}
			else {

				var config = {
					transformRequest: angular.identity,
					transformResponse: angular.identity,
					headers: {
						'Content-Type': undefined
					}
				}

				var data = new FormData();
				data.append('file', $scope.ficheroEnviar);
				data.append('receptor', $scope.salaActual.otroUsuario);
				$scope.cargando = true;
				$http.post("/chat/sendfile", data, config).then(
					function sucessCallback(response) {
						if (response.status == 200) {
							var name = JSON.parse(response.data);
							$scope.mensajes.push({ contenido: name.titulo, deMentor: soyMentor, sala: $scope.salaActual.id, deTexto: false });
							$notification.success("Fichero enviado con éxito", null, null, false);
							$scope.cargando = false;

						}

					},
					function errorCallback(response) {
						var mensaje = JSON.parse(response.data);
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
								$notification.error(mensaje.titulo, mensaje.mensaje, null, false);
								break;
						}
						$scope.cargando = false;
						errorSound();
					}
				)
			}
		}
		else {
			$notification.error("Sin fichero", "Por favor, seleccione un fichero antes de darle a enviar", null, false);
			errorSound();
		}
	}

	$scope.dowloadFile = function(file) {
		$scope.cargando = true;
		var path = "";
		if (soyMentor == file.deMentor) {
			path = "/file/download/chat/my/" + $scope.salaActual.id + "/" + file.contenido;
			window.open(path, '_blank', '');
		}
		else if (soyMentor) {
			path = "/file/download/mentorizado/chat/" + $scope.salaActual.id + "/" + file.contenido;
			//https://stackoverflow.com/questions/29747136/download-a-file-using-angular-js-and-a-spring-based-restful-web-service
			window.open(path, '_blank', '');
		}
		else {
			path = "/file/download/mentor/chat/" + $scope.salaActual.id + "/" + file.contenido;
			window.open(path, '_blank', '');
		}

		$scope.cargando = false;

	}

	$scope.borrarFile = function(mensaje) {

		var config = {
			transformRequest: angular.identity,
			transformResponse: angular.identity,
			headers: {
				'Content-Type': undefined
			}
		}

		var data = new FormData();
		data.append('file', mensaje.contenido);
		data.append('sala', $scope.salaActual.id);
		$scope.cargando = true;
		$http.post("/chat/deletefile", data, config).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					var name = JSON.parse(response.data);
					mensaje.contenido = name.titulo;
					mensaje.deTexto = true;
					$notification.success("Fichero borrado con éxito", null, null, false);
					$scope.cargando = false;

				}

			},
			function errorCallback(response) {
				var mensaje = JSON.parse(response.data);
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
						$notification.error(mensaje.titulo, mensaje.mensaje, null, false);
						break;
				}
				$scope.cargando = false;
				errorSound();
			}
		)
	}

});