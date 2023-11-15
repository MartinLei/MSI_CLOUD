#create VM Instances
gcloud compute instances create backend-1 backend-2 --zone=europe-west3-c --machine-type=e2-medium --source-machine-image=backend --project=spring-monolith-403010 --service-account=702381394218-compute@developer.gserviceaccount.com

#create Instance Groups
gcloud compute instance-groups unmanaged create instance-group-auto --project=spring-monolith-403010 --zone=europe-west3-c
gcloud compute instance-groups unmanaged set-named-ports instance-group-auto --project=spring-monolith-403010 --zone=europe-west3-c --named-ports=backendport:9000
gcloud compute instance-groups unmanaged add-instances instance-group-auto --project=spring-monolith-403010 --zone=europe-west3-c --instances=backend-1
gcloud compute instance-groups unmanaged add-instances instance-group-auto --project=spring-monolith-403010 --zone=europe-west3-c --instances=backend-2

#create Healthcheck
gcloud beta compute health-checks create http autohealth --project=spring-monolith-403010 --port=9000 --request-path=/ --proxy-header=NONE --no-enable-logging --check-interval=5 --timeout=5 --unhealthy-threshold=2 --healthy-threshold=2

# create Backend for Load Balancer
gcloud compute backend-services create autobackend --load-balancing-scheme=EXTERNAL --protocol=HTTP --port-name=http --health-checks=autohealth --global
gcloud compute backend-services add-backend autobackend --instance-group=instance-group-auto --instance-group-zone=europe-west3-c --global
gcloud compute backend-services update autobackend --global --port-name=backendport

# create Routing for Load Balancer
gcloud compute url-maps create automap --default-service autobackend
gcloud compute target-http-proxies create autoproxy --url-map=automap

# reserve IP Adress for Load Balancer
gcloud compute addresses create lb-ipv4-1 --ip-version=IPV4 --network-tier=PREMIUM --global

# Finalize Load Balancer
gcloud compute forwarding-rules create http-content-auto --load-balancing-scheme=EXTERNAL --address=lb-ipv4-1 --global --target-http-proxy=autoproxy --ports=80

# Print IP Adress for Load Balancer
gcloud compute addresses describe lb-ipv4-1 --global

echo "Done ----- Check the printed address"