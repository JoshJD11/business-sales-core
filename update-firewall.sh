az sql server firewall-rule create \
  --resource-group business-sales-rg \
  --server joshuaroot \
  --name MiPC \
  --start-ip-address $(curl -s ifconfig.me) \
  --end-ip-address $(curl -s ifconfig.me)