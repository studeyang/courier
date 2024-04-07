dir('courier-consumer') {
    runDockerBuild(
            'appName': 'courier-consumer',
            'dockerRegistry': 'swr.cn-south-1.myhuaweicloud.com',
            'dockerImage': 'studeyang/courier-consumer'
    )
}

dir('courier-agent') {
    runDockerBuild(
            'appName': 'courier-agent',
            'dockerRegistry': 'swr.cn-south-1.myhuaweicloud.com',
            'dockerImage': 'studeyang/courier-agent'
    )
}

dir('courier-producer') {
    runDockerBuild(
            'appName': 'courier-producer',
            'dockerRegistry': 'swr.cn-south-1.myhuaweicloud.com',
            'dockerImage': 'studeyang/courier-producer'
    )
}

dir('courier-delay') {
    runDockerBuild(
            'appName': 'courier-delay',
            'dockerRegistry': 'swr.cn-south-1.myhuaweicloud.com',
            'dockerImage': 'studeyang/courier-delay'
    )
}

dir('courier-management') {
    runDockerBuild(
            'appName': 'courier-management',
            'dockerRegistry': 'swr.cn-south-1.myhuaweicloud.com',
            'dockerImage': 'studeyang/courier-management'
    )
}

dir('web-courier-admin') {
    runDockerBuild(
            'appName': 'web-courier-admin',
            'dockerRegistry': 'swr.cn-south-1.myhuaweicloud.com',
            'dockerImage': 'studeyang/web-courier-admin'
    )
}
