stage('Build & push images') {
  sh 'make build-kuma push-kuma'
}

stage('Deploy') {
  env.KUBECONFIG = "${env.HOME}/.kube/virginia.kube_config"
  env.DEIS_PROFILE = 'virginia'
  env.DEIS_BIN = 'deis2'
  env.DEIS_APP = 'mdn-' + env.BRANCH_NAME

  sh 'make deis-create'
  // sh 'make deis-config'
  // currently broken:
  // DEIS_PROFILE=virginia deis2 config:push -p .env-dist -a mdn-demo
  // Creating config... ...o..Error: Unknown Error (409): {"detail":"jenkins changed nothing - release stopped"}
  // make: *** [deis-config] Error 1
  sh "KUBECONFIG=${env.KUBECONFIG} kubectl --namespace=${env.DEIS_APP} apply -f k8s/"
  sh 'make deis-pull'
  sh 'make deis-migrate'
  sh 'make deis-scale-worker'
}
