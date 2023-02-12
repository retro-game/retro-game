'use strict';

$(document).on('keydown', function (event) {
  switch (event.which) {
    case 37: // left arrow
      if($('#top-bar-prev > button').is(':enabled')){
        $('#top-bar-prev').submit();
      }
      break;
    case 39: // right arrow
      if($('#top-bar-next > button').is(':enabled')){
        $('#top-bar-next').submit();
      }
      break;
  }
});