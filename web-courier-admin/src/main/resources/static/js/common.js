$(function () {

    var slideToTop = $("<div />");
    slideToTop.html('<i class="fa fa-chevron-up"></i>');
    slideToTop.css({
        position: 'fixed',
        bottom: '20px',
        right: '25px',
        width: '40px',
        height: '40px',
        color: '#eee',
        'font-size': '',
        'line-height': '40px',
        'text-align': 'center',
        'background-color': '#222d32',
        cursor: 'pointer',
        'border-radius': '5px',
        'z-index': '99999',
        opacity: '.7',
        'display': 'none'
    });

    slideToTop.on('mouseenter', function () {
        $(this).css('opacity', '1');
    });

    slideToTop.on('mouseout', function () {
        $(this).css('opacity', '.7');
    });

    $('.wrapper').append(slideToTop);
    $(window).scroll(function () {
        if ($(window).scrollTop() >= 150) {
            if (!$(slideToTop).is(':visible')) {
                $(slideToTop).fadeIn(500);
            }
        } else {
            $(slideToTop).fadeOut(500);
        }
    });

    $(slideToTop).click(function () {
        $("html,body").animate({
            scrollTop: 0
        }, 100);
    });

    $('.sidebar-toggle').click(function () {
        var xxljob_adminlte_settings = $.cookie('xxljob_adminlte_settings');	// on=open，off=close
        if ('off' == xxljob_adminlte_settings) {
            xxljob_adminlte_settings = 'on';
        } else {
            xxljob_adminlte_settings = 'off';
        }
        $.cookie('xxljob_adminlte_settings', xxljob_adminlte_settings, {expires: 7});	//$.cookie('the_cookie', '', { expires: -1 });
    });
});
