# Modify this IP to match your server
serverAddress="10.72.21.42"

# Add all SSH addresses on which constellation will run.
# To  avoid being asked for a password each time, setup SSH keys.
#
# Example:
# computeAddresses=(\
# "odroid@10.72.20.59" \
# "odroid@10.72.21.38" \
#)

# Add all the compute nodes to this list
computeAddresses=(\
    "odroid-1" \
    "odroid-2" \
)

# Add the target
targetAddress="zaklaw01@10.72.21.42"

# Add the source
sourceAddress="zaklaw01@10.72.21.42"