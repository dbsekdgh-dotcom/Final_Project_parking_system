import React, { useEffect } from 'react'
import {searchFeePolicy} from './../api/feeApi'
import { useQuery } from '@tanstack/react-query'

const FeePolicy = () => {
  
  const {data,isError,isLoading}=useQuery({
    queryKey:['feePolicy',FeePolicy],
    queryFn:async()=>await searchFeePolicy(),
  })

  return (
    <div>

    </div>
  )
}

export default FeePolicy;