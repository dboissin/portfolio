
(function($) {
  var reload, error;

  reload = function() {
    window.location.reload(true);
  }

  error = function(request, jqstatus, httpstatus) {
    $(this.errorid).append('<p>' + request.status + ' ' + httpstatus + '</p>')
                   .show();
  }

  $(function() {

    $(".create-form").submit(function() {
      $.ajax({
        type: "POST",
        url: $(this).attr("action"),
        data: $(this).serialize(),
        context: {
          errorid: '#error'
        }
      })
      .done(reload)
      .fail(error);
      return false;
    });

    $("#import-photos").submit(function() {
      var source = new EventSource(
        "/photos/import?" +
        "path=" + $("#photos-folder").val() + "&" +
        "galleries=" + $("#categories").val() + "&" +
        "categories=" + $("#galleries").val()
      );
      source.onmessage = function(event) {
        console.log(event.data);
      }
      return false;
    });

  });
})(jQuery);

