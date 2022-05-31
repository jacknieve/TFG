var appConsumer = angular.module('appConsumer', ['ngAnimate', 'notifications']);
var snd_error = new Audio("../sounds/beep-5.wav");

function errorSound() {
	snd_error.play();
}

appConsumer.config(function($httpProvider) {
	//Enable cross domain calls
	$httpProvider.defaults.useXDomain = true;
});

appConsumer.controller("globalController", function($rootScope) {
	$rootScope.popUpAbierto = false;



});

appConsumer.controller("notificacionController", function($scope, $http, $notification) {

	$scope.mostrarsin = false;
	$scope.cargando = false;
	$scope.enfallo = false;
	var stompClient = null;
	var miUsername = "";


	var obtenerMiInfo = function() {
		$http.get("/user/miinfo").then(
			function sucessCallback(response) {
				miUsername = response.data.username;
				$scope.iniciaNotificaciones();
			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 401:
						$notification.error("Sin autorización", "No tienes permiso para obtener la información de tu cuenta en esta pantalla", null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener la información de tu cuenta para relaizar las acciones del chat" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
				}
				$scope.enfallo = true;
			}
		)
	}

	$scope.iniciaNotificaciones = function() {
		$scope.cargando = false;
		$http.get("/user/notificaciones").then(
			function sucessCallback(response) {
				$scope.enfallo = false;
				if (response.status == 200) {
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
					//console.log('Connected: ' + frame);
					stompClient.subscribe("/usuario/" + miUsername + "/queue/messages", controladorMensajes);
				});
				$scope.cargando = false;
			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para obtener las notificaciones," +
							" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
							+ new Date(), null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio que contiene las notificaciones, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener las notificaciones, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las notificaciones" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
				}
				$scope.enfallo = true;
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
					index = $scope.notificaciones.indexOf(notificacion);
					$scope.notificaciones.splice(index, 1);
					if ($scope.notificaciones.length == 0) {
						$scope.mostrarsin = true;
					}
				}
				$scope.cargando = false;

			},
			function errorCallback(response) {
				$scope.enfallo = true;
				if (response.status == 0) {
					$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
				}
				else $notification.error(response.data.titulo, response.data.mensaje, null, false);
				$scope.cargando = false;
				errorSound();
			}
		)
	}

	var controladorMensajes = function(mensaje) {
		var mensaje = JSON.parse(mensaje.body);
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




