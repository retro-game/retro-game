'use strict';

$(document).on('keydown', function (event) {
  if (event.which === 32) { // space
    $('#phalanx-reload form').submit();
  }
});
