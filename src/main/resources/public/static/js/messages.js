'use strict';

$('[data-delete-message]').click(function () {
  var elem = $(this);
  var bodyId = +elem.attr('data-body');
  var messageId = +elem.attr('data-id');
  $.ajax({
    type: 'post',
    url: '/messages/delete',
    contentType: 'application/json',
    data: JSON.stringify({
      'bodyId': bodyId,
      'messageId': messageId
    }),
    success: function (data) {
      if (data.success) {
        elem.parent().parent().parent().remove();
      } else {
        alert('Deleting the message failed');
      }
    },
    error: function () {
      alert('Deleting the message failed');
    }
  });
});
