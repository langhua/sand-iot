import axios from 'axios'
import { useRoute } from 'vue-router'
const route = useRoute()

const details = axios({
            method: 'POST',
            headers: { 'content-type': 'application/x-www-form-urlencoded' },
            data: 'type=' + route.params.type + '&base64Name=' + route.params.base64Name,
            url: 'http://sandflower:8080/iot/control/getMdnsDetails',
            //  timeout: 10000,
          })
          .then(response => {
                console.log(response.data)
                return response.data
          })

export default details
