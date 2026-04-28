pipeline {
    agent any
    
    environment {
        AWS_REGION = 'ap-northeast-2'
        ECR_REGISTRY = '079092240156.dkr.ecr.ap-northeast-2.amazonaws.com'
        BACKEND_IMAGE = "${ECR_REGISTRY}/parking-backend"
        AI_IMAGE = "${ECR_REGISTRY}/parking-ai"
        SERVER_1_ID = 'i-019f4c49b578a0a28'
        SERVER_2_ID = 'i-039b40dc35867bbaf'
        TG_ARN = 'arn:aws:elasticloadbalancing:ap-northeast-2:079092240156:targetgroup/tg-parking-backend/9e1caa6a2b242dc7'
        ADMIN_BUCKET = 'parking-frontend-admin'
        USER_BUCKET = 'parking-frontend-user'
        KIOSK_BUCKET = 'parking-frontend-kiosk'
        ADMIN_CF_ID = 'E11EOPU9PR3ODN'
        USER_CF_ID = 'E1IRC5OJEGAFW8'
        KIOSK_CF_ID = 'EEQOV34NAOFWV'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git credentialsId: 'github-credentials',
                    url: 'https://github.com/dbsekdgh-dotcom/Final_Project_parking_system.git',
                    branch: 'new-branch-bokyung'
            }
        }
        
        stage('ECR Login') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    """
                }
            }
        }
        
        stage('Build & Push Backend') {
            steps {
                sh """
                    docker build --no-cache -t ${BACKEND_IMAGE}:latest ./backend/parking_backend
                    docker push ${BACKEND_IMAGE}:latest
                """
            }
        }
        
        stage('Build & Push AI') {
            steps {
                sh """
                    docker build -t ${AI_IMAGE}:latest ./ai-service
                    docker push ${AI_IMAGE}:latest
                """
            }
        }

        stage('Build & Deploy Frontend') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        # Admin 빌드 및 배포
                        cd ./frontend/parking_frontend_admin
                        npm ci
                        npm run build
                        aws s3 sync dist/ s3://${ADMIN_BUCKET} --delete
                        aws cloudfront create-invalidation --distribution-id ${ADMIN_CF_ID} --paths "/*"
                        cd ../..

                        # User 빌드 및 배포
                        cd ./frontend/parking_frontend_user
                        npm ci
                        npm run build
                        aws s3 sync dist/ s3://${USER_BUCKET} --delete
                        aws cloudfront create-invalidation --distribution-id ${USER_CF_ID} --paths "/*"
                        cd ../..

                        # Kiosk 빌드 및 배포
                        cd ./frontend/parking_frontend_kiosk
                        npm ci
                        npm run build
                        aws s3 sync dist/ s3://${KIOSK_BUCKET} --delete
                        aws cloudfront create-invalidation --distribution-id ${KIOSK_CF_ID} --paths "/*"
                        cd ../..
                    """
                }
            }
        }
        
        stage('Deploy to Server 2') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                        script {
                            def cmdId = sh(script: """
                                aws ssm send-command \
                                    --instance-ids ${SERVER_2_ID} \
                                    --document-name "AWS-RunShellScript" \
                                    --parameters '{"commands":["export HOME=/root && cd /home/ssm-user/Final_Project_parking_system && git pull https://${GIT_TOKEN}@github.com/dbsekdgh-dotcom/Final_Project_parking_system.git new-branch-bokyung && aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin ${ECR_REGISTRY} && docker compose pull && docker rm -f parking-backend && docker compose up -d && docker image prune -f"]}' \
                                    --region ${AWS_REGION} \
                                    --query 'Command.CommandId' \
                                    --output text
                            """, returnStdout: true).trim()
                            
                            sh """
                                aws ssm wait command-executed \
                                    --command-id ${cmdId} \
                                    --instance-id ${SERVER_2_ID} \
                                    --region ${AWS_REGION}
                            """
                        }
                    }
                }
            }
        }
        
        stage('Health Check Server 2') {
            steps {
                sh 'sleep 30'
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws elbv2 describe-target-health \
                            --target-group-arn ${TG_ARN} \
                            --region ${AWS_REGION}
                    """
                }
            }
        }
        
        stage('Switch Traffic to Server 2') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws elbv2 register-targets \
                            --target-group-arn ${TG_ARN} \
                            --targets Id=${SERVER_2_ID} \
                            --region ${AWS_REGION}
                        aws elbv2 deregister-targets \
                            --target-group-arn ${TG_ARN} \
                            --targets Id=${SERVER_1_ID} \
                            --region ${AWS_REGION}
                    """
                }
            }
        }
        
        stage('Deploy to Server 1') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                        script {
                            def cmdId = sh(script: """
                                aws ssm send-command \
                                    --instance-ids ${SERVER_1_ID} \
                                    --document-name "AWS-RunShellScript" \
                                    --parameters '{"commands":["export HOME=/root && cd /home/ssm-user/Final_Project_parking_system && git pull https://${GIT_TOKEN}@github.com/dbsekdgh-dotcom/Final_Project_parking_system.git new-branch-bokyung && aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin ${ECR_REGISTRY} && docker compose pull && docker rm -f parking-backend && docker compose up -d && docker image prune -f"]}' \
                                    --region ${AWS_REGION} \
                                    --query 'Command.CommandId' \
                                    --output text
                            """, returnStdout: true).trim()
                            
                            sh """
                                aws ssm wait command-executed \
                                    --command-id ${cmdId} \
                                    --instance-id ${SERVER_1_ID} \
                                    --region ${AWS_REGION}
                            """
                        }
                    }
                }
            }
        }
        
        stage('Switch Traffic to Both') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws elbv2 register-targets \
                            --target-group-arn ${TG_ARN} \
                            --targets Id=${SERVER_1_ID} \
                            --region ${AWS_REGION}
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo '배포 성공!'
        }
        failure {
            echo '배포 실패!'
        }
    }
}