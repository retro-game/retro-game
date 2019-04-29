'use strict';

function prettyNumber(n) {
  var lang = document.documentElement.lang || 'en';
  return n.toLocaleString(lang);
}

function prettyDate(t) {
  var date = new Date(t * 1000);
  var now = new Date();
  var s = '';
  if (now.getDate() !== date.getDate() || now.getMonth() !== date.getMonth() || now.getFullYear() !== date.getFullYear()) {
    s += date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2) + ' ';
  }
  return s + ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2) + ':' + ('0' + date.getSeconds()).slice(-2);
}

function prettyTime(t) {
  var seconds = t % 60;
  t = Math.floor(t / 60);
  var minutes = t % 60;
  t = Math.floor(t / 60);
  var hours = t % 24;
  var days = Math.floor(t / 24);
  return (days > 0 ? days + ' d ' : '') + ('0' + hours).slice(-2) + ':' + ('0' + minutes).slice(-2) + ':' + ('0' + seconds).slice(-2);
}

function getResources() {
  var names = ['metal', 'crystal', 'deuterium'];
  var resources = {};
  for (var i = 0; i < names.length; i++) {
    var name = names[i];
    resources[name] = +$('[data-resources-' + name + ']').attr('data-resources-' + name);
  }
  return resources;
}

$(function () {
  if ($(document.body).attr('data-number-input-scrolling') === 'false') {
    $(document).on("wheel", "input[type=number]", function () {
      $(this).blur();
    });
  }

  $('#top-bar-body-list :input').change(function () {
    $('#top-bar-body-list').submit();
  });

  var timers = $('[data-timer]');
  if (timers.length > 0) {
    var updateTimers = function () {
      var now = new Date().getTime() / 1000 | 0;
      for (var i = 0; i < timers.length; i++) {
        var timer = timers[i];
        var t = +$(timer).attr('data-timer');
        var diff = t - now;
        if (diff <= 0) {
            document.location.reload();
            return;
        }
        timer.innerHTML = prettyTime(diff) + '<br><span class="date">' + prettyDate(t) + '</span>';
      }
    };
    updateTimers();
    setInterval(updateTimers, 100);
  }

  $('[data-set]').click(function () {
    var input = $('#' + $(this).attr('data-set-for'));
    input.val($(this).attr('data-set-value'));
    input.trigger('change');
    return false;
  });

  $('[data-tooltip]').each(function () {
    var self = $(this);
    new Tooltip(this, {
      html: self.attr('data-tooltip-html') !== undefined,
      title: self.attr('data-tooltip-title')
    })
  });
});
