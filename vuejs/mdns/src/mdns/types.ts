import axios from 'axios'

const types = await axios.post('http://sandflower:8080/iot/control/getMdnsTypes')
    .then(response => {
      console.log(JSON.parse(response.data.mdnsTypes))
      return JSON.parse(response.data.mdnsTypes)
    })

export default types

