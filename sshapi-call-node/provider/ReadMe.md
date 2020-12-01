SSHApi-Call-Node Plugin:

Parameters List that managed in from Directed Graphs:

Input:
Url    ->    Mandatory    ->    url to make the SSH connection request to.
Port    ->    Mandatory    ->    port to make the SSH connection request to.
AuthType    ->    Optional    ->    Type of authentiation to be used BASIC or sshKey based    ->    true or false
User    ->    Optional    ->    user name to use for ssh basic authentication    ->    sdnc_ws
Password    ->    Optional    ->    unencrypted password to use for ssh basic authentication    ->    plain_password
SshKey    ->    Optional    ->    Consumer SSH key to use for ssh authentication    ->    plain_key
ExecTimeout    ->    Optional    ->    SSH command execution timeout    ->    plain_key
Retry    ->    Optional    ->    Make ssh connection with default retry policy    ->    plain_key
Cmd    ->    Mandatory    ->    ssh command to be executed on the server.    ->    get post put delete patch
EnvParameters    ->    Optional    ->    A JSON dictionary which should list key value pairs to be passed to the command execution. These values would correspond to instance specific parameters that a command may need to execute an action.
FileParameters    ->    Optional    ->    A JSON dictionary where keys are filenames and values are contents of files. The SSH Server will utilize this feature to generate files with keys as filenames and values as content. This attribute can be used to generate files that a command may require as part of execution.
ConvertResponse     ->    Optional    ->    whether the response should be converted to properties   ->    true or false
ResponseType    ->    Optional    ->    If we know the response is to be in a specific format (supported are JSON, XML and NONE) 
ResponsePrefix    ->    Optional    ->    location the response will be written to in context memory
listName[i]    ->    Optional    ->    Used for processing XML responses with repeating elements.</td>vpn-information.vrf-details

Output:
"sshApi.call.node.status"   ->    SSH Exit status code is set in here.
"sshApi.call.node.stdout"   ->    SSH command execution result is put in here. 
"sshApi.call.node.stderr"   ->    SSH execution failure message is put in here. 



API methods that are exposed:
1) execCommand
2) execWithStatusCheck: Throws exception if the exit status is not successful.
3) execCommandWithPty