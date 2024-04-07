checkAndRunMaven(
        'mvnActions': ['clean', '-U', 'deploy', '-N']
)

dir('courier-core') {
    checkAndRunMaven(
            'mvnActions': ['clean', '-U', 'deploy']
    )
}

dir('courier-client') {
    checkAndRunMaven(
            'mvnActions': ['clean', '-U', 'deploy']
    )
}

dir('courier-repository') {
    checkAndRunMaven(
            'mvnActions': ['clean', '-U', 'deploy']
    )
}

dir('courier-commons') {
    checkAndRunMaven(
            'mvnActions': ['clean', '-U', 'deploy']
    )
}

dir('courier-spring-boot-starter') {
    checkAndRunMaven(
            'mvnActions': ['clean', '-U', 'deploy']
    )
}

dir('courier-consumer') {
    runMaven(
            'mvnActions': ['clean', '-U', 'install']
    )
}

dir('courier-agent') {
    runMaven(
            'mvnActions': ['clean', '-U', 'install']
    )
}

dir('courier-producer') {
    runMaven(
            'mvnActions': ['clean', '-U', 'install']
    )
}

dir('courier-delay') {
    runMaven(
            'mvnActions': ['clean', '-U', 'install']
    )
}

dir('courier-management') {
    runMaven(
            'mvnActions': ['clean', '-U', 'install']
    )
}

dir('web-courier-admin') {
    runMaven(
            'mvnActions': ['clean', '-U', 'install']
    )
}
