var appConsumer = angular.module('appConsumer', ['notifications']);

var snd_error = new Audio("../sounds/beep-5.wav");

function errorSound() {
	snd_error.play();
}

appConsumer.config(function($httpProvider) {
	//Enable cross domain calls
	$httpProvider.defaults.useXDomain = true;
});

//https://o7planning.org/11673/spring-boot-file-upload-with-angularjs
// DIRECTIVE - FILE MODEL
appConsumer.directive('fileModel', ['$parse', function($parse) {
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


appConsumer.controller("userController", function($scope, $http, $notification, $window) {

	//Mapa para acceder directamente a las areas de usuario
	const areasUsuario = new Map();
	//Mapa para ver si un area es nueva al borrar, es decir, si aun no se ha guardado en el backend
	const areasNuevas = new Map();
	var copiaDatos;
	$scope.borrar = false;
	$scope.confirmarBorrar = false;
	$scope.cargando = false;
	$scope.password = "";
	$scope.popupAbierto = false;
	$scope.errorObtener = false;
	$scope.avisoRefrescar = false;
	var stompClient = null;
	$scope.imagenPerfil = null;
	$scope.foto = "/images/usuario.png";
	$scope.ficheroSubir = null;
	$scope.sinficherosperfil = false;
	$scope.mydate = null;
	$scope.conexionPerdida = false;

	$scope.getInfo = function() {
		$scope.cargando = true;
		$http.get("/user/info").then(
			function sucessCallback(response) {
				if (response.status == 200) {
					$scope.usuario = response.data;
					copiaDatos = Object.assign({}, response.data);
					if(response.data.fnacimiento != null){
						$scope.mydate = new Date(response.data.fnacimiento);
					}
					//Aqui pasamos las areas a un mapa, para acceder directamente al añadir o borrar
					if (response.data.foto != "") {
						$scope.foto = response.data.foto;
					}
					if (response.data.areas.length > 0) {
						for (var i = 0; i < response.data.areas.length; i++) {
							areasUsuario.set(response.data.areas[i].area, response.data.areas[i]);
						}
					}
					else {
						$scope.sinareas = true;
					}
					if (response.data.ficheros.length == 0) {
						$scope.sinficherosperfil = true;
					}
					$scope.areaseleccioanda = "--Escoge una--";
					var socket = new SockJS('/websocket');
					stompClient = Stomp.over(socket);
					stompClient.connect({}, function(frame) {
						//console.log('Connected: ' + frame);
						stompClient.subscribe("/usuario/" + response.data.correo + "/queue/messages", controladorMensajes);
					});
					socket.onclose = function() {
					console.log("Cerrado conexion");
					$notification.warning("Conexión perdida", "Se ha perdido la conexión con el servidor.", null, false);
					$scope.conexionPerdida = true;
					stompClient.disconnect();
					$scope.$apply();
				}
				}
				$scope.cargando = false;
			},
			function errorCallback(response) {
				switch (response.status) {
					case 0:
						$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
						break;
					case 400:
						$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para obtener su información," +
							" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
							+ new Date(), null, false);
						break;
					case 503:
						$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio para obtener su información, por favor" +
							"vuelva a intentarlo más tarde", null, false);
						break;
					case 500:
						$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar obtener su información, si recibe este error, por favor, pongase en contacto con "
							+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					case 401:
						$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
						break;
					case 404:
						$notification.error("Sin chat", "No se ha encontrado la información de su cuenta" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					case 403:
						$notification.error("Usuario no encontrado", "Se ha producido un fallo al intentar acceder a la información de tu cuenta" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
					default:
						$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener su información" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
						break;
				}
				$scope.cargando = false;
			}
		)
	}


	$scope.getInfo();



	var controladorMensajes = function(mensaje) {
		var mensaje = JSON.parse(mensaje.body);
		switch (mensaje.asunto) {
			case "NOTIFICACION":
				$notification.info(mensaje.cuerpo.titulo, "", null, false);
				break;
			case "ERROR":
				$notification.error(mensaje.cuerpo.titulo, mensaje.cuerpo.descripcion, null, false);
				break;
		}
		$scope.$apply();
	}

	$scope.setInfo = function(usuario) {
		if ($scope.form.$valid) {
			$scope.cargando = true;
			usuario.fnacimiento = $scope.mydate;
			$http.post("/user/setinfo", usuario).then(
				function sucessCallback(response) {
					areasNuevas.clear(); //Borramos las areas para no intentar solo eliminar del frontend un area
					$notification.success("Información actualizada", "Tu información se ha actualizadp de forma exitosa", null, false);
					$scope.cargando = false;
				},
				function errorCallback(response) {
					switch (response.status) {
						case 0:
							$notification.error("Servidor no disponible", "En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias", null, false);
							break;
						case 400:
							$notification.error("Fallo en la solicitud", "Se ha producido un fallo en la petición al servidor para actualizar su información," +
								" si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: "
								+ new Date(), null, false);
							break;
						case 503:
							$notification.error("Fallo en el repositorio", "Se ha producido un fallo al intentar acceder al repositorio para actualizar su información, por favor" +
								"vuelva a intentarlo más tarde", null, false);
							break;
						case 500:
							$notification.error("Error interno", "Se ha producido un fallo interno en el servidor al intentar actualizar su información, si recibe este error, por favor, pongase en contacto con "
								+ "nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
							break;
						case 401:
							$notification.error("Sin autorización", "No tienes permiso para realizar esta acción.", null, false);
							break;
						case 403:
							$notification.error("Usuario no encontrado", "Se ha producido un fallo al intentar acceder a la información de tu cuenta para actualizarla" +
								", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
							break;
						case 404:
							$notification.error("Sin chat", "En el servidor no hay constancia de un chat abierto con este mentor" +
								", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
							break;
						default:
							$notification.error("Otro error", "Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar actualizar su información" +
								", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error. Hora del suceso: " + new Date(), null, false);
							break;
					}
					$scope.cargando = false;
					errorSound();
				}
			)
		}
		else {
			errorSound();
			$notification.error("Valores no válidos", "Por favor, introduzca valores válidos en los campos", null, false);
		}
	}

	$scope.deshacer = function() {
		$scope.usuario = Object.assign({}, copiaDatos);
	}

	//Cambiarlo a add area
	$scope.addArea = function(areaSelecionada) {
		$scope.cargando = true;
		$scope.sinareas = false;
		if (areaSelecionada !== "--Escoge una--") {
			if (areasUsuario.has(areaSelecionada)) {
				$notification.error("Área ya seleccionada", "Ya has seleccionado previamente ese área.", null, false);
				errorSound();
			}
			else {
				$scope.usuario.areas.push({ area: areaSelecionada });
				areasUsuario.set(areaSelecionada, $scope.usuario.areas[$scope.usuario.areas.length - 1]);
				//Esto lo usamos para no tener que llamar a borrar cuando aun no se ha guardado un area
				areasNuevas.set(areaSelecionada, areaSelecionada);
			}
		}
		$scope.cargando = false;
	}


	$scope.borrarArea = function(area) {
		$scope.cargando = true;
		if (areasNuevas.has(area.area)) {
			index = $scope.usuario.areas.indexOf(area);
			$scope.usuario.areas.splice(index, 1);
			areasUsuario.delete(area.area);
			areasNuevas.delete(area.area);
			if ($scope.usuario.areas.length == 0) {
				$scope.sinareas = true;
			}
			$scope.cargando = false;
		}
		else {
			$http.post("/user/areas/delete", area).then(
				function sucessCallback(response) {
					if (response.status == 200) {
						index = $scope.usuario.areas.indexOf(area.area);
						$scope.usuario.areas.splice(index, 1);
						areasUsuario.delete(area.area);
						if ($scope.usuario.areas.length == 0) {
							$scope.sinareas = true;
						}
						$scope.cargando = false;
					}

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
	}



	$scope.abrirBorrar = function() {
		if ($scope.borrar) {
			$scope.borrar = false
			$scope.popupAbierto = false;
		}
		else {
			$scope.borrar = true;
			$scope.popupAbierto = true;
		}
	}

	$scope.cancelarConfirmar = function() {
		$scope.borrar = false;
		$scope.confirmarBorrar = false;
		$scope.popupAbierto = false;
	}

	$scope.abrirConfirmar = function() {
		if ($scope.password != '') {
			$scope.confirmarBorrar = true;
		}
	}

	$scope.subirFoto = function() {
		if ($scope.imagenPerfil != null) {
			const size = ($scope.imagenPerfil.size / 1024) / 1024;
			if (size > 15.0) {
				$notification.error("Imagen demasiado grande", "El tamaño de la imagen excede lo permitido (15MB)", null, false);
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
				data.append('imagen', $scope.imagenPerfil);
				$scope.cargando = true;
				$http.post("/file/fotoperfil", data, config).then(
					function sucessCallback(response) {
						if (response.status == 200) {
							var path = JSON.parse(response.data);
							$scope.foto = "/images/usuario.png";
							$scope.foto = path.titulo;
							$notification.success("Imagen subida con éxito", null, null, false);
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
			$notification.error("Sin imagen", "Por favor, seleccione una imagen antes de darle a aceptar", null, false);
			errorSound();
		}
	};

	$scope.subirFichero = function() {
		if ($scope.ficheroSubir != null) {
			if ($scope.usuario.ficheros.indexOf($scope.ficheroSubir.name) == -1) {


				const size = ($scope.ficheroSubir.size / 1024) / 1024;
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
					data.append('file', $scope.ficheroSubir);
					$scope.cargando = true;
					$http.post("/file/uploadfile", data, config).then(
						function sucessCallback(response) {
							if (response.status == 200) {
								$scope.sinficherosperfil = false;
								var name = JSON.parse(response.data);
								$scope.usuario.ficheros.push(name.titulo);
								$notification.success("Fichero subido con éxito", null, null, false);
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
			} else {
				$notification.error("Fichero duplicado", "Ya tienes subido un fichero con el mismo nombre y formato", null, false);
				errorSound();
			}
		}
		else {
			$notification.error("Sin fichero", "Por favor, seleccione un fichero antes de darle a aceptar", null, false);
			errorSound();
		}
	}

	$scope.dowloadFile = function(file) {
		$scope.cargando = true;
		path = "/file/download/my/" + file;
		window.open(path, '_blank', '');

		$scope.cargando = false;

	}

	$scope.borrarFile = function(file) {
		$scope.cargando = true;
		$http.post("/file/deletefile", file).then(
			function sucessCallback(response) {
				if (response.status == 200) {
					index = $scope.usuario.ficheros.indexOf(file);
					$scope.usuario.ficheros.splice(index, 1);
					if ($scope.usuario.ficheros.length == 0) {
						$scope.sinficherosperfil = true;
					}
					$notification.success("Fichero eliminado con éxito", null, null, false);
					$scope.cargando = false;
				}

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



});
