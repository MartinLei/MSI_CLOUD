gcloud compute forwarding-rules delete http-content-auto --global -q

gcloud compute addresses delete lb-ipv4-1 --global -q

gcloud compute target-http-proxies delete autoproxy -q

gcloud compute url-maps delete automap -q

gcloud compute backend-services delete autobackend --global -q

gcloud beta compute health-checks delete autohealth --project=spring-monolith-403010 -q

gcloud compute instance-groups unmanaged delete instance-group-auto --zone=europe-west3-c -q

gcloud compute instances delete backend-1 backend-2 --zone=europe-west3-c -q

echo "Deletion Successful"